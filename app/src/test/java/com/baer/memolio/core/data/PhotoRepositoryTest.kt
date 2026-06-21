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
class PhotoRepositoryTest {

    private lateinit var db: MemolioDatabase
    private lateinit var repo: PhotoRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemolioDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = PhotoRepositoryImpl(db.photoDao(), UnconfinedTestDispatcher())
        db.albumDao().let { dao ->
            // album row required for FK
        }
    }

    @After
    fun teardown() = db.close()

    @Test
    fun addedPhotoAppearsInAlbumStream() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        repo.add(
            id = "p1", originalPath = "/photos/p1.jpg",
            displayCachePath = "/cache/display/p1.jpg", thumbPath = "/cache/thumb/p1.jpg",
            contentHash = "h1", width = 100, height = 100, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = "phone", addedAt = 1L
        )
        repo.observePhotos("a1").test {
            assertThat(awaitItem().map { it.id }).containsExactly("p1")
        }
    }

    @Test
    fun isDuplicateReflectsExistingHash() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        repo.add(
            id = "p1", originalPath = "/photos/p1.jpg",
            displayCachePath = "/c/d/p1.jpg", thumbPath = "/c/t/p1.jpg",
            contentHash = "dup", width = 1, height = 1, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 1L
        )
        assertThat(repo.isDuplicate("dup")).isTrue()
        assertThat(repo.isDuplicate("other")).isFalse()
    }

    @Test
    fun softDeleteThenRestoreRoundTrips() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        repo.add(
            id = "p1", originalPath = "/photos/p1.jpg",
            displayCachePath = "/c/d/p1.jpg", thumbPath = "/c/t/p1.jpg",
            contentHash = "h", width = 1, height = 1, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 1L
        )
        repo.softDelete("p1", now = 50L)
        repo.observePhotos("a1").test { assertThat(awaitItem()).isEmpty() }
        repo.restore("p1")
        repo.observePhotos("a1").test { assertThat(awaitItem().map { it.id }).containsExactly("p1") }
    }

    @Test
    fun freshlyAddedPhotoHasNullFocalPoint() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        repo.add(
            id = "p1", originalPath = "/photos/p1.jpg",
            displayCachePath = "/c/d/p1.jpg", thumbPath = "/c/t/p1.jpg",
            contentHash = "h2", width = 100, height = 100, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 2L
        )
        repo.observePhotos("a1").test {
            val photo = awaitItem().single()
            assertThat(photo.focalX).isNull()
            assertThat(photo.focalY).isNull()
        }
    }

    @Test
    fun setFocalPointRoundTrips() = runTest {
        db.albumDao().upsert(AlbumEntity("a1", "All", null, 0L, 0))
        repo.add(
            id = "p1", originalPath = "/photos/p1.jpg",
            displayCachePath = "/c/d/p1.jpg", thumbPath = "/c/t/p1.jpg",
            contentHash = "h3", width = 100, height = 100, orientation = 0,
            caption = null, albumId = "a1", sourceDevice = null, addedAt = 3L
        )
        repo.setFocalPoint("p1", x = 0.3f, y = 0.7f)
        repo.observePhotos("a1").test {
            val photo = awaitItem().single()
            assertThat(photo.focalX).isWithin(0.0001f).of(0.3f)
            assertThat(photo.focalY).isWithin(0.0001f).of(0.7f)
        }
    }
}
