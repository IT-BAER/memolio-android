package com.baer.memolio.work

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.baer.memolio.core.data.PhotoRepositoryImpl
import com.baer.memolio.core.database.AlbumEntity
import com.baer.memolio.core.database.MemolioDatabase
import com.baer.memolio.core.media.FaceDetector
import com.baer.memolio.core.media.FocalPoint
import com.baer.memolio.core.storage.FileStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
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
class FaceBackfillWorkerTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var db: MemolioDatabase
    private lateinit var repo: PhotoRepositoryImpl
    private lateinit var storage: FileStorage

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemolioDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = PhotoRepositoryImpl(db.photoDao(), UnconfinedTestDispatcher())
        storage = FileStorage(tmp.root)
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
    }

    @After
    fun teardown() = db.close()

    private suspend fun addPhoto(id: String, focalX: Float? = null, focalY: Float? = null): File {
        val displayFile = storage.displayCacheFile(id)
        displayFile.parentFile?.mkdirs()
        displayFile.writeBytes(byteArrayOf(1, 2, 3)) // dummy bytes; fake detector ignores content
        repo.add(
            id = id,
            originalPath = storage.originalFile(id, "jpg").path,
            displayCachePath = displayFile.path,
            thumbPath = storage.thumbFile(id).path,
            contentHash = id,
            width = 1, height = 1, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 0L
        )
        if (focalX != null && focalY != null) {
            repo.setFocalPoint(id, focalX, focalY)
        }
        return displayFile
    }

    private fun worker(fakeDetector: FaceDetector): FaceBackfillWorker =
        TestListenableWorkerBuilder<FaceBackfillWorker>(ApplicationProvider.getApplicationContext())
            .setWorkerFactory(
                FaceBackfillWorkerFactory(
                    photoRepository = repo,
                    faceDetector = fakeDetector,
                    ioDispatcher = UnconfinedTestDispatcher()
                )
            )
            .build()

    @Test
    fun setsDetectedFocalPointForPhotoWithNullFocal() = runTest {
        addPhoto("p1") // no focal point
        val detectedFp = FocalPoint(0.4f, 0.3f)
        val fakeDetector = FakeAlwaysFaceDetector(detectedFp)

        val result = worker(fakeDetector).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        val photo = repo.observeAllLivePhotos().first().first { it.id == "p1" }
        assertThat(photo.focalX).isWithin(0.001f).of(0.4f)
        assertThat(photo.focalY).isWithin(0.001f).of(0.3f)
    }

    @Test
    fun skipsPhotoThatAlreadyHasFocalPoint() = runTest {
        addPhoto("p2", focalX = 0.5f, focalY = 0.5f)
        val fakeDetector = CountingFaceDetector(FocalPoint(0.9f, 0.9f))

        val result = worker(fakeDetector).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        // Detector must not have been called for p2
        assertThat(fakeDetector.callCount).isEqualTo(0)
        // Focal remains original
        val photo = repo.observeAllLivePhotos().first().first { it.id == "p2" }
        assertThat(photo.focalX).isWithin(0.001f).of(0.5f)
        assertThat(photo.focalY).isWithin(0.001f).of(0.5f)
    }

    @Test
    fun skipsPhotoWhoseDisplayCacheFileMissing() = runTest {
        // Insert a photo whose displayCachePath points to a non-existent file
        val missingPath = File(tmp.root, "cache/display/missing.jpg").path
        repo.add(
            id = "p3",
            originalPath = storage.originalFile("p3", "jpg").path,
            displayCachePath = missingPath,
            thumbPath = storage.thumbFile("p3").path,
            contentHash = "p3",
            width = 1, height = 1, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 0L
        )
        val fakeDetector = CountingFaceDetector(FocalPoint(0.5f, 0.5f))

        val result = worker(fakeDetector).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakeDetector.callCount).isEqualTo(0)
        // Focal still null
        val photo = repo.observeAllLivePhotos().first().first { it.id == "p3" }
        assertThat(photo.focalX).isNull()
    }

    @Test
    fun returnsSuccessAndSetsDetectedOnlyForNullFocalWhenMixed() = runTest {
        addPhoto("pa") // needs backfill
        addPhoto("pb", focalX = 0.2f, focalY = 0.8f) // already set
        val detectedFp = FocalPoint(0.6f, 0.7f)
        val fakeDetector = CountingFaceDetector(detectedFp)

        val result = worker(fakeDetector).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        // Only "pa" was processed
        assertThat(fakeDetector.callCount).isEqualTo(1)
        val pa = repo.observeAllLivePhotos().first().first { it.id == "pa" }
        assertThat(pa.focalX).isWithin(0.001f).of(0.6f)
        val pb = repo.observeAllLivePhotos().first().first { it.id == "pb" }
        assertThat(pb.focalX).isWithin(0.001f).of(0.2f)
    }

    // --- helpers ---

    private class FakeAlwaysFaceDetector(private val fp: FocalPoint) : FaceDetector {
        override suspend fun detectFocalPoint(jpeg: java.io.File): FocalPoint = fp
    }

    private class CountingFaceDetector(private val fp: FocalPoint) : FaceDetector {
        var callCount = 0
        override suspend fun detectFocalPoint(jpeg: java.io.File): FocalPoint {
            callCount++
            return fp
        }
    }
}
