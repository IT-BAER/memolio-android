package com.baer.memolio.feature.manage.library

import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.model.Photo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class LibraryGatingTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeAlbumRepo : AlbumRepository {
        val albums = MutableStateFlow<List<Album>>(emptyList())
        override fun observeAlbums(): Flow<List<Album>> = albums
        override suspend fun upsert(album: Album) { albums.value = albums.value.filterNot { it.id == album.id } + album }
        override suspend fun delete(id: String) { albums.value = albums.value.filterNot { it.id == id } }
    }

    private class FakePhotoRepo : PhotoRepository {
        override fun observePhotos(albumId: String): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observeTrash(): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override suspend fun isDuplicate(contentHash: String) = false
        override suspend fun add(id: String, originalPath: String, displayCachePath: String, thumbPath: String, contentHash: String, width: Int, height: Int, orientation: Int, caption: String?, albumId: String, sourceDevice: String?, addedAt: Long) {}
        override suspend fun softDelete(id: String, now: Long) {}
        override suspend fun restore(id: String) {}
        override suspend fun purgeTrashOlderThan(threshold: Long) = 0
        override suspend fun moveToAlbum(id: String, albumId: String) {}
        override suspend fun setFavorite(id: String, favorite: Boolean) {}
        override suspend fun setCaption(id: String, caption: String?) {}
        override suspend fun reorder(orderedIds: List<String>) {}
    }

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: android.app.Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

    @Test
    fun freeUserCannotCreateAlbum() = runTest {
        val albumRepo = FakeAlbumRepo()
        val vm = LibraryViewModel(albumRepo, FakePhotoRepo(), FakeEntitlement(pro = false), dispatcher) { 100L }
        vm.createAlbum("Holiday")
        assertThat(albumRepo.albums.value).isEmpty()
    }

    @Test
    fun proUserCanCreateAlbum() = runTest {
        val albumRepo = FakeAlbumRepo()
        val vm = LibraryViewModel(albumRepo, FakePhotoRepo(), FakeEntitlement(pro = true), dispatcher) { 100L }
        vm.createAlbum("Holiday")
        assertThat(albumRepo.albums.value.single().name).isEqualTo("Holiday")
    }
}
