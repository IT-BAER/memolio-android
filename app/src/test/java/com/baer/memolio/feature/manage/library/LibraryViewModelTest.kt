package com.baer.memolio.feature.manage.library

import app.cash.turbine.test
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.model.Photo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class LibraryViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeAlbumRepository : AlbumRepository {
        val albums = MutableStateFlow<List<Album>>(emptyList())
        override fun observeAlbums(): Flow<List<Album>> = albums
        override suspend fun upsert(album: Album) {
            albums.value = albums.value.filterNot { it.id == album.id } + album
        }
        override suspend fun delete(id: String) {
            albums.value = albums.value.filterNot { it.id == id }
        }
    }

    private class FakePhotoRepository : PhotoRepository {
        val photosByAlbum = MutableStateFlow<Map<String, List<Photo>>>(emptyMap())
        var lastMoved: Pair<String, String>? = null
        var lastFavorite: Pair<String, Boolean>? = null
        var lastDeleted: String? = null
        var lastReorder: List<String>? = null
        override fun observePhotos(albumId: String): Flow<List<Photo>> =
            photosByAlbum.map { it[albumId].orEmpty() }
        override fun observeTrash(): Flow<List<Photo>> = flowOf(emptyList())
        override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> = flowOf(emptyList())
        override suspend fun isDuplicate(contentHash: String): Boolean = false
        override suspend fun add(
            id: String, originalPath: String, displayCachePath: String, thumbPath: String,
            contentHash: String, width: Int, height: Int, orientation: Int,
            caption: String?, albumId: String, sourceDevice: String?, addedAt: Long
        ) {}
        override suspend fun softDelete(id: String, now: Long) { lastDeleted = id }
        override suspend fun restore(id: String) {}
        override suspend fun purgeTrashOlderThan(threshold: Long): Int = 0
        override suspend fun moveToAlbum(id: String, albumId: String) { lastMoved = id to albumId }
        override suspend fun setFavorite(id: String, favorite: Boolean) { lastFavorite = id to favorite }
        override suspend fun setCaption(id: String, caption: String?) {}
        override suspend fun reorder(orderedIds: List<String>) { lastReorder = orderedIds }
    }

    private fun photo(id: String, album: String) = Photo(
        id = id, originalPath = "", displayCachePath = "", thumbPath = "", contentHash = id,
        width = 1, height = 1, orientation = 0, caption = null, albumId = album,
        favorite = false, sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = null
    )

    @Test
    fun albumsAreExposedFromRepository() = runTest {
        val albumRepo = FakeAlbumRepository()
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), dispatcher) { 100L }
        albumRepo.albums.value = listOf(Album("a1", "Family", null, 0L, 0))

        vm.state.test {
            // Under UnconfinedTestDispatcher the initial empty state can be conflated
            // with the populated one, so accept either emission order.
            var item = awaitItem()
            if (item.albums.isEmpty()) item = awaitItem()
            assertThat(item.albums.map { it.name }).containsExactly("Family")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createAlbumUpsertsWithGeneratedId() = runTest {
        val albumRepo = FakeAlbumRepository()
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), dispatcher) { 100L }

        vm.createAlbum("Holiday")

        assertThat(albumRepo.albums.value.single().name).isEqualTo("Holiday")
        assertThat(albumRepo.albums.value.single().createdAt).isEqualTo(100L)
    }

    @Test
    fun renameAlbumKeepsIdAndUpdatesName() = runTest {
        val albumRepo = FakeAlbumRepository()
        albumRepo.albums.value = listOf(Album("a1", "Old", null, 5L, 2))
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), dispatcher) { 100L }

        vm.renameAlbum(Album("a1", "Old", null, 5L, 2), "New")

        val a = albumRepo.albums.value.single()
        assertThat(a.id).isEqualTo("a1")
        assertThat(a.name).isEqualTo("New")
        assertThat(a.createdAt).isEqualTo(5L)
        assertThat(a.sortOrder).isEqualTo(2)
    }

    @Test
    fun deleteAlbumRemovesIt() = runTest {
        val albumRepo = FakeAlbumRepository()
        albumRepo.albums.value = listOf(Album("a1", "X", null, 0L, 0))
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), dispatcher) { 100L }

        vm.deleteAlbum("a1")

        assertThat(albumRepo.albums.value).isEmpty()
    }

    @Test
    fun openingAlbumExposesItsPhotos() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1"), photo("p2", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, dispatcher) { 100L }

        vm.openAlbum("a1")

        vm.state.test {
            assertThat(awaitItem().openAlbumPhotos.map { it.id }).containsExactly("p1", "p2")
        }
    }

    @Test
    fun selectionTogglesAndBatchActionsCallRepository() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1"), photo("p2", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, dispatcher) { 100L }
        vm.openAlbum("a1")

        vm.toggleSelection("p1")
        vm.toggleSelection("p2")
        vm.state.test { assertThat(awaitItem().selectedIds).containsExactly("p1", "p2") }

        vm.favoriteSelected(true)
        assertThat(photoRepo.lastFavorite).isEqualTo("p2" to true)

        vm.moveSelectedTo("a2")
        assertThat(photoRepo.lastMoved).isEqualTo("p2" to "a2")

        vm.deleteSelected()
        assertThat(photoRepo.lastDeleted).isEqualTo("p2")

        vm.state.test { assertThat(awaitItem().selectedIds).isEmpty() }
    }

    @Test
    fun reorderDelegatesToRepository() = runTest {
        val photoRepo = FakePhotoRepository()
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, dispatcher) { 100L }

        vm.reorder(listOf("p3", "p1", "p2"))

        assertThat(photoRepo.lastReorder).containsExactly("p3", "p1", "p2").inOrder()
    }
}
