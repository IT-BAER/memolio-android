package com.baer.memolio.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PhotoDaoTest {

    private lateinit var db: MemolioDatabase
    private lateinit var photoDao: PhotoDao
    private lateinit var albumDao: AlbumDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemolioDatabase::class.java
        ).allowMainThreadQueries().build()
        photoDao = db.photoDao()
        albumDao = db.albumDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun observeLivePhotosExcludesTrashed() = runTest {
        albumDao.upsert(AlbumEntity("a1", "All", null, 0L, 0))
        photoDao.upsert(photoEntity("p1", deletedAt = null))
        photoDao.upsert(photoEntity("p2", deletedAt = 5L))

        photoDao.observeLivePhotos("a1").test {
            val live = awaitItem()
            assertThat(live.map { it.id }).containsExactly("p1")
        }
    }

    @Test
    fun softDeleteMovesPhotoToTrash() = runTest {
        albumDao.upsert(AlbumEntity("a1", "All", null, 0L, 0))
        photoDao.upsert(photoEntity("p1", deletedAt = null))

        photoDao.softDelete("p1", deletedAt = 99L)

        photoDao.observeTrash().test {
            assertThat(awaitItem().map { it.id }).containsExactly("p1")
        }
    }

    @Test
    fun slideshowPoolExcludesHiddenAndTrashed() = runTest {
        albumDao.upsert(AlbumEntity("a1", "All", null, 0L, 0))
        photoDao.upsert(photoEntity("p1", deletedAt = null))
        photoDao.upsert(photoEntity("p2", deletedAt = null))
        photoDao.upsert(photoEntity("p3", deletedAt = null))
        photoDao.updateInPlaylist("p3", false)

        photoDao.observeSlideshowPool().test {
            assertThat(awaitItem().map { it.id }).containsExactly("p1", "p2")
        }

        photoDao.softDelete("p1", deletedAt = 42L)

        photoDao.observeSlideshowPool().test {
            assertThat(awaitItem().map { it.id }).containsExactly("p2")
        }
    }

    @Test
    fun slideshowInAlbumsExcludesHidden() = runTest {
        albumDao.upsert(AlbumEntity("a1", "All", null, 0L, 0))
        photoDao.upsert(photoEntity("p1", deletedAt = null))
        photoDao.upsert(photoEntity("p2", deletedAt = null))
        photoDao.upsert(photoEntity("p3", deletedAt = null))
        photoDao.updateInPlaylist("p3", false)

        photoDao.observeSlideshowInAlbums(setOf("a1")).test {
            assertThat(awaitItem().map { it.id }).containsExactly("p1", "p2")
        }
    }

    private fun photoEntity(id: String, deletedAt: Long?) = PhotoEntity(
        id = id,
        originalPath = "/photos/$id.jpg",
        displayCachePath = "/cache/display/$id.jpg",
        thumbPath = "/cache/thumb/$id.jpg",
        contentHash = id,
        width = 100, height = 100, orientation = 0,
        caption = null, albumId = "a1", favorite = false,
        sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = deletedAt
    )
}
