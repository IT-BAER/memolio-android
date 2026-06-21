package com.baer.memolio.core.media

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.data.PhotoRepositoryImpl
import com.baer.memolio.core.database.AlbumEntity
import com.baer.memolio.core.database.MemolioDatabase
import com.baer.memolio.core.media.Transcoder.Decoded
import com.baer.memolio.core.storage.FileStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class MediaImporterTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var db: MemolioDatabase
    private lateinit var baseRepo: PhotoRepositoryImpl
    private lateinit var repo: CapturingPhotoRepository
    private lateinit var storage: FileStorage
    private lateinit var importer: MediaImporter

    /** Records calls and writes a placeholder JPEG so the cache files exist. */
    private class FakeTranscoder : Transcoder {
        val writes = mutableListOf<Pair<File, Int>>() // dest -> maxEdge
        override fun readBounds(source: File): Decoded? = Decoded(4000, 3000)
        override fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int): Decoded {
            writes += dest to maxEdge
            dest.parentFile?.mkdirs()
            dest.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))
            val (w, h) = ImageScaler.targetSize(4000, 3000, maxEdge)
            return Decoded(w, h)
        }
    }

    /** Controllable FaceDetector: set [result] before each test. */
    private class FakeFaceDetector(var result: FocalPoint? = null) : FaceDetector {
        override suspend fun detectFocalPoint(jpeg: File): FocalPoint? = result
    }

    /** Wraps a real PhotoRepository and captures setFocalPoint calls. */
    private class CapturingPhotoRepository(
        private val delegate: PhotoRepository
    ) : PhotoRepository by delegate {
        /** Last recorded (id, x, y) from setFocalPoint, or null if never called. */
        var lastFocal: Triple<String, Float, Float>? = null

        override suspend fun setFocalPoint(id: String, x: Float, y: Float) {
            lastFocal = Triple(id, x, y)
            delegate.setFocalPoint(id, x, y)
        }
    }

    private val fakeTranscoder = FakeTranscoder()
    private val fakeDetector = FakeFaceDetector()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemolioDatabase::class.java
        ).allowMainThreadQueries().build()
        val dispatcher = UnconfinedTestDispatcher()
        baseRepo = PhotoRepositoryImpl(db.photoDao(), dispatcher)
        repo = CapturingPhotoRepository(baseRepo)
        storage = FileStorage(tmp.root)
        importer = MediaImporter(fakeTranscoder, storage, repo, fakeDetector, dispatcher)
    }

    @After
    fun teardown() = db.close()

    @Test
    fun importWritesOriginalDisplayCacheThumbAndInsertsRow() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        val bytes = "the-photo-bytes".toByteArray()

        val result = importer.import(bytes, "jpg", "a1", caption = "Hello", sourceDevice = "iphone", now = 7L)

        val expectedId = Hasher.sha256(bytes).take(32)
        assertThat(result).isEqualTo(MediaImporter.ImportResult.Added(expectedId))
        assertThat(storage.originalFile(expectedId, "jpg").readBytes()).isEqualTo(bytes)
        assertThat(storage.displayCacheFile(expectedId).exists()).isTrue()
        assertThat(storage.thumbFile(expectedId).exists()).isTrue()
        // display cache at 2560, thumb at 480
        assertThat(fakeTranscoder.writes.map { it.second }).containsExactly(2560, 480).inOrder()

        repo.observePhotos("a1").test {
            val photos = awaitItem()
            assertThat(photos).hasSize(1)
            assertThat(photos[0].id).isEqualTo(expectedId)
            assertThat(photos[0].caption).isEqualTo("Hello")
            assertThat(photos[0].sourceDevice).isEqualTo("iphone")
            assertThat(photos[0].width).isEqualTo(2560)   // display dims recorded
            assertThat(photos[0].height).isEqualTo(1920)
            assertThat(photos[0].addedAt).isEqualTo(7L)
        }
    }

    @Test
    fun importSecondTimeReturnsDuplicateAndDoesNotInsertTwice() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        val bytes = "dup-bytes".toByteArray()

        val first = importer.import(bytes, "jpg", "a1", null, null, now = 1L)
        val second = importer.import(bytes, "jpg", "a1", null, null, now = 2L)

        assertThat(first).isInstanceOf(MediaImporter.ImportResult.Added::class.java)
        assertThat(second).isEqualTo(MediaImporter.ImportResult.Duplicate)
        repo.observePhotos("a1").test {
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun importRejectsUndecodableBytes() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        // Transcoder that cannot read bounds -> not an image
        val nullBounds = object : Transcoder {
            override fun readBounds(source: File): Decoded? = null
            override fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int): Decoded =
                error("must not be called")
        }
        val rejectingImporter = MediaImporter(nullBounds, storage, repo, fakeDetector, UnconfinedTestDispatcher())

        val result = rejectingImporter.import("garbage".toByteArray(), "txt", "a1", null, null, now = 1L)

        assertThat(result).isInstanceOf(MediaImporter.ImportResult.Rejected::class.java)
        repo.observePhotos("a1").test { assertThat(awaitItem()).isEmpty() }
    }

    @Test
    fun focalPointPersistedWhenDetectorReturnsPoint() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        fakeDetector.result = FocalPoint(0.3f, 0.7f)
        val bytes = "focal-photo-bytes".toByteArray()

        val result = importer.import(bytes, "jpg", "a1", null, null, now = 10L)

        val expectedId = Hasher.sha256(bytes).take(32)
        assertThat(result).isEqualTo(MediaImporter.ImportResult.Added(expectedId))
        assertThat(repo.lastFocal).isEqualTo(Triple(expectedId, 0.3f, 0.7f))
    }

    @Test
    fun focalPointNotPersistedWhenDetectorReturnsNull() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        fakeDetector.result = null
        val bytes = "no-face-photo-bytes".toByteArray()

        val result = importer.import(bytes, "jpg", "a1", null, null, now = 11L)

        val expectedId = Hasher.sha256(bytes).take(32)
        assertThat(result).isEqualTo(MediaImporter.ImportResult.Added(expectedId))
        assertThat(repo.lastFocal).isNull()
        // photo still inserted
        repo.observePhotos("a1").test { assertThat(awaitItem()).hasSize(1) }
    }
}
