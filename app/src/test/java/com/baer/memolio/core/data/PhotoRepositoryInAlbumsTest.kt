package com.baer.memolio.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.baer.memolio.core.database.AlbumEntity
import com.baer.memolio.core.database.MemolioDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PhotoRepositoryInAlbumsTest {

    private lateinit var db: MemolioDatabase
    private lateinit var repo: PhotoRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemolioDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = PhotoRepositoryImpl(db.photoDao(), UnconfinedTestDispatcher())
    }

    @After
    fun teardown() = db.close()

    private suspend fun seed() {
        db.albumDao().upsert(AlbumEntity("a1", "Trips", null, 0L, 0))
        db.albumDao().upsert(AlbumEntity("a2", "Family", null, 0L, 1))
        db.albumDao().upsert(AlbumEntity("a3", "Hidden", null, 0L, 2))
        repo.add("p1", "/o/p1.jpg", "/d/p1.jpg", "/t/p1.jpg", "h1", 1, 1, 0, null, "a1", null, 1L)
        repo.add("p2", "/o/p2.jpg", "/d/p2.jpg", "/t/p2.jpg", "h2", 1, 1, 0, null, "a2", null, 2L)
        repo.add("p3", "/o/p3.jpg", "/d/p3.jpg", "/t/p3.jpg", "h3", 1, 1, 0, null, "a3", null, 3L)
        // p4 lives in a1 but is trashed — must be excluded
        repo.add("p4", "/o/p4.jpg", "/d/p4.jpg", "/t/p4.jpg", "h4", 1, 1, 0, null, "a1", null, 4L)
        repo.softDelete("p4", now = 99L)
    }

    @Test
    fun unionsLivePhotosAcrossSelectedAlbums() = runTest {
        seed()
        repo.observePhotosInAlbums(setOf("a1", "a2")).test {
            val ids = awaitItem().map { it.id }
            assertThat(ids).containsExactly("p1", "p2")
            assertThat(ids).doesNotContain("p3") // a3 not selected
            assertThat(ids).doesNotContain("p4") // trashed
        }
    }

    @Test
    fun observeAllLivePhotosReturnsWholeLivePoolAcrossAlbums() = runTest {
        seed()
        repo.observeAllLivePhotos().test {
            val ids = awaitItem().map { it.id }
            assertThat(ids).containsExactly("p1", "p2", "p3") // every album, live only
            assertThat(ids).doesNotContain("p4") // trashed excluded
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptyAlbumSetReturnsEmptyListWithoutQuerying() = runTest {
        seed()
        repo.observePhotosInAlbums(emptySet()).test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
