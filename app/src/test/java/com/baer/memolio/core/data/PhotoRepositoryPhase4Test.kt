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
class PhotoRepositoryPhase4Test {

    private lateinit var db: MemolioDatabase
    private lateinit var repo: PhotoRepositoryImpl

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

    private suspend fun seed(id: String, albumId: String, sortOrder: Int = 0) {
        repo.add(
            id = id, originalPath = "/photos/$id.jpg",
            displayCachePath = "/c/d/$id.jpg", thumbPath = "/c/t/$id.jpg",
            contentHash = id, width = 1, height = 1, orientation = 0,
            caption = null, albumId = albumId, sourceDevice = null, addedAt = sortOrder.toLong()
        )
    }

    @Test
    fun moveToAlbumRelocatesPhoto() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "A1", null, 0L, 0))
        db.albumDao().upsert(AlbumEntity("a2", "A2", null, 0L, 1))
        seed("p1", "a1")

        repo.moveToAlbum("p1", "a2")

        repo.observePhotos("a1").test { assertThat(awaitItem()).isEmpty() }
        repo.observePhotos("a2").test { assertThat(awaitItem().map { it.id }).containsExactly("p1") }
    }

    @Test
    fun setFavoriteUpdatesFlag() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "A1", null, 0L, 0))
        seed("p1", "a1")

        repo.setFavorite("p1", true)

        repo.observePhotos("a1").test {
            assertThat(awaitItem().single().favorite).isTrue()
        }
    }

    @Test
    fun setCaptionUpdatesAndClears() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "A1", null, 0L, 0))
        seed("p1", "a1")

        repo.setCaption("p1", "Beach 2024")
        repo.observePhotos("a1").test {
            assertThat(awaitItem().single().caption).isEqualTo("Beach 2024")
        }

        repo.setCaption("p1", null)
        repo.observePhotos("a1").test {
            assertThat(awaitItem().single().caption).isNull()
        }
    }

    @Test
    fun setInPlaylistTogglesPhotoInSlideshowPool() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "A1", null, 0L, 0))
        seed("p1", "a1")
        seed("p2", "a1")

        repo.setInPlaylist("p1", false)

        repo.observeSlideshowPool().test {
            assertThat(awaitItem().map { it.id }).containsExactly("p2")
        }

        repo.setInPlaylist("p1", true)

        repo.observeSlideshowPool().test {
            assertThat(awaitItem().map { it.id }).containsExactly("p1", "p2")
        }
    }

    @Test
    fun observeSlideshowInAlbumsEmptySetReturnsEmpty() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "A1", null, 0L, 0))
        seed("p1", "a1")

        repo.observeSlideshowInAlbums(emptySet()).test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reorderAppliesNewSortOrderInOrderOfList() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "A1", null, 0L, 0))
        seed("p1", "a1", sortOrder = 0)
        seed("p2", "a1", sortOrder = 1)
        seed("p3", "a1", sortOrder = 2)

        repo.reorder(listOf("p3", "p2", "p1"))

        repo.observePhotos("a1").test {
            assertThat(awaitItem().map { it.id }).containsExactly("p3", "p2", "p1").inOrder()
        }
    }
}
