package com.baer.memolio.work

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.baer.memolio.appliance.PurgeThreshold
import com.baer.memolio.appliance.TimeProvider
import com.baer.memolio.core.data.PhotoRepositoryImpl
import com.baer.memolio.core.database.AlbumEntity
import com.baer.memolio.core.database.MemolioDatabase
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.storage.FileStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrashPurgeWorkerTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var db: MemolioDatabase
    private lateinit var repo: PhotoRepositoryImpl
    private lateinit var storage: FileStorage
    private val dayMillis = 24L * 60 * 60 * 1000
    private val fixedNow = 100L * dayMillis

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

    private suspend fun addTrashed(id: String, deletedAt: Long) {
        repo.add(
            id = id, originalPath = storage.originalFile(id, "jpg").path,
            displayCachePath = storage.displayCacheFile(id).path,
            thumbPath = storage.thumbFile(id).path,
            contentHash = id, width = 1, height = 1, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 0L
        )
        repo.softDelete(id, deletedAt)
        storage.writeOriginal(id, "jpg") { it.write(byteArrayOf(1)) }
    }

    private fun worker(autoCleanup: Boolean): TrashPurgeWorker =
        TestListenableWorkerBuilder<TrashPurgeWorker>(ApplicationProvider.getApplicationContext())
            .setWorkerFactory(
                TrashPurgeWorkerFactory(
                    photoRepository = repo,
                    fileStorage = storage,
                    settings = flowOf(AppSettings(autoCleanup = autoCleanup)),
                    time = TimeProvider { 0 },
                    nowMillis = { fixedNow }
                )
            )
            .build()

    @Test
    fun purgesTrashOlderThanThirtyDaysWhenAutoCleanupOn() = runTest {
        addTrashed("old", deletedAt = fixedNow - 31 * dayMillis)
        addTrashed("fresh", deletedAt = fixedNow - 5 * dayMillis)

        val result = worker(autoCleanup = true).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(db.photoDao().existsByHash("old")).isFalse()
        assertThat(storage.originalFile("old", "jpg").exists()).isFalse()
        assertThat(db.photoDao().existsByHash("fresh")).isTrue()
        assertThat(storage.originalFile("fresh", "jpg").exists()).isTrue()
    }

    @Test
    fun doesNothingWhenAutoCleanupOff() = runTest {
        addTrashed("old", deletedAt = fixedNow - 31 * dayMillis)

        val result = worker(autoCleanup = false).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(db.photoDao().existsByHash("old")).isTrue()
        assertThat(storage.originalFile("old", "jpg").exists()).isTrue()
    }

    @Test
    fun thresholdMatchesPurgeThresholdCalc() {
        assertThat(PurgeThreshold.cutoff(fixedNow)).isEqualTo(fixedNow - 30 * dayMillis)
    }
}
