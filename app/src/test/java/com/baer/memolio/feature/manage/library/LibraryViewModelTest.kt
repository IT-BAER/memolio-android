package com.baer.memolio.feature.manage.library

import app.cash.turbine.test
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

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: android.app.Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

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
        var lastInPlaylist: Pair<String, Boolean>? = null
        val inPlaylistCalls = mutableListOf<Pair<String, Boolean>>()
        override fun observePhotos(albumId: String): Flow<List<Photo>> =
            photosByAlbum.map { it[albumId].orEmpty() }
        override fun observeTrash(): Flow<List<Photo>> = flowOf(emptyList())
        override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> = flowOf(emptyList())
        override fun observeAllLivePhotos(): Flow<List<Photo>> =
            photosByAlbum.map { it.values.flatten() }
        override fun observeSlideshowPool(): Flow<List<Photo>> =
            photosByAlbum.map { it.values.flatten() }
        override fun observeSlideshowInAlbums(albumIds: Set<String>): Flow<List<Photo>> = flowOf(emptyList())
        override suspend fun setInPlaylist(id: String, inPlaylist: Boolean) {
            lastInPlaylist = id to inPlaylist
            inPlaylistCalls += id to inPlaylist
        }
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
        override suspend fun setFocalPoint(id: String, x: Float, y: Float) {}
    }

    private fun photo(id: String, album: String, inPlaylist: Boolean = true) = Photo(
        id = id, originalPath = "", displayCachePath = "", thumbPath = "/t/$id.jpg", contentHash = id,
        width = 1, height = 1, orientation = 0, caption = null, albumId = album,
        favorite = false, inPlaylist = inPlaylist, sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = null
    )

    @Test
    fun albumsAreExposedFromRepository() = runTest {
        val albumRepo = FakeAlbumRepository()
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), FakeEntitlement(true), dispatcher) { 100L }
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
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), FakeEntitlement(true), dispatcher) { 100L }

        vm.createAlbum("Holiday")

        assertThat(albumRepo.albums.value.single().name).isEqualTo("Holiday")
        assertThat(albumRepo.albums.value.single().createdAt).isEqualTo(100L)
    }

    @Test
    fun renameAlbumKeepsIdAndUpdatesName() = runTest {
        val albumRepo = FakeAlbumRepository()
        albumRepo.albums.value = listOf(Album("a1", "Old", null, 5L, 2))
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), FakeEntitlement(true), dispatcher) { 100L }

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
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), FakeEntitlement(true), dispatcher) { 100L }

        vm.deleteAlbum("a1")

        assertThat(albumRepo.albums.value).isEmpty()
    }

    @Test
    fun openingAlbumExposesItsPhotos() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1"), photo("p2", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }

        vm.openAlbum("a1")

        vm.state.test {
            assertThat(awaitItem().openAlbumPhotos.map { it.id }).containsExactly("p1", "p2")
        }
    }

    @Test
    fun closeAlbumReturnsToAlbumListAndClearsPhotos() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.state.test {
            // Settle on the fully-opened album (id + photos resolved through the
            // combine; intermediate emissions can carry a stale photo list).
            var s = awaitItem()
            while (s.openAlbumId != "a1" || s.openAlbumPhotos.size != 1) s = awaitItem()

            vm.closeAlbum()
            while (s.openAlbumId != null || s.openAlbumPhotos.isNotEmpty()) s = awaitItem()
            assertThat(s.openAlbumId == null).isTrue()
            assertThat(s.openAlbumPhotos).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun openingBlankAlbumIsTreatedAsClosed() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        // Open a real album first, then a blank id must reset back to the list (not "").
        vm.openAlbum("a1")
        vm.state.test {
            var s = awaitItem()
            while (s.openAlbumId != "a1") s = awaitItem()

            vm.openAlbum("")
            while (s.openAlbumId != null) s = awaitItem()
            assertThat(s.openAlbumId == null).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun albumCoverResolvesFromFirstPhoto() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1"), photo("p2", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.state.test {
            var s = awaitItem()
            while (s.albumCovers["a1"] == null) s = awaitItem()
            assertThat(s.albumCovers["a1"]).isEqualTo("/t/p1.jpg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun openAllPhotosExposesWholePoolAcrossAlbums() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf(
            "a1" to listOf(photo("p1", "a1")),
            "a2" to listOf(photo("p2", "a2"))
        )
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }

        vm.openAllPhotos()

        vm.state.test {
            var s = awaitItem()
            while (!s.isAllPhotosOpen || s.openAlbumPhotos.size != 2) s = awaitItem()
            assertThat(s.openAlbumId).isEqualTo(ALL_PHOTOS_ID)
            assertThat(s.openAlbumPhotos.map { it.id }).containsExactly("p1", "p2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun perPhotoActionsCallRepository() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }

        vm.favorite("p1", true)
        assertThat(photoRepo.lastFavorite).isEqualTo("p1" to true)

        vm.setInPlaylist("p1", false)
        assertThat(photoRepo.lastInPlaylist).isEqualTo("p1" to false)

        vm.deletePhoto("p1")
        assertThat(photoRepo.lastDeleted).isEqualTo("p1")
    }

    @Test
    fun previewOpensAndResolvesPhotoThenCloses() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1"), photo("p2", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.openPreview("p2")

        vm.state.test {
            var s = awaitItem()
            while (s.previewPhoto?.id != "p2") s = awaitItem()
            assertThat(s.previewPhotoId).isEqualTo("p2")

            vm.closePreview()
            while (s.previewPhotoId != null) s = awaitItem()
            assertThat(s.previewPhoto == null).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletingPreviewedPhotoClosesThePreview() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.openPreview("p1")
        vm.state.test {
            var s = awaitItem()
            while (s.previewPhotoId != "p1") s = awaitItem()

            vm.deletePhoto("p1")
            while (s.previewPhotoId != null) s = awaitItem()
            assertThat(s.previewPhotoId == null).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetViewReturnsToAlbumListAndClearsPreview() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.openPreview("p1")
        vm.state.test {
            var s = awaitItem()
            while (s.openAlbumId != "a1" || s.previewPhotoId != "p1") s = awaitItem()

            vm.resetView()
            while (s.openAlbumId != null || s.previewPhotoId != null) s = awaitItem()
            assertThat(s.openAlbumId == null).isTrue()
            assertThat(s.previewPhotoId == null).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteOpenAlbumReassignsPhotosToAllThenRemovesAlbum() = runTest {
        val albumRepo = FakeAlbumRepository()
        albumRepo.albums.value = listOf(Album("fam", "Family", null, 0L, 1))
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("fam" to listOf(photo("p1", "fam"), photo("p2", "fam")))
        val vm = LibraryViewModel(albumRepo, photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("fam")

        vm.deleteOpenAlbum()

        // Photos were moved to the All-photos bucket (id "all") before the album was removed.
        assertThat(photoRepo.lastMoved).isEqualTo("p2" to ALL_PHOTOS_ID)
        assertThat(albumRepo.albums.value).isEmpty()
    }

    @Test
    fun deleteOpenAlbumNeverDeletesTheAllPhotosBucket() = runTest {
        val albumRepo = FakeAlbumRepository()
        albumRepo.albums.value = listOf(Album(ALL_PHOTOS_ID, "All photos", null, 0L, 0))
        val vm = LibraryViewModel(albumRepo, FakePhotoRepository(), FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum(ALL_PHOTOS_ID)

        vm.deleteOpenAlbum()

        assertThat(albumRepo.albums.value.map { it.id }).containsExactly(ALL_PHOTOS_ID)
    }

    @Test
    fun selectionTogglesAndEntersSelectionMode() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1"), photo("p2", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")

        vm.toggleSelection("p1")
        vm.toggleSelection("p2")
        vm.state.test {
            var s = awaitItem()
            while (s.selectedIds.size != 2) s = awaitItem()
            assertThat(s.selectedIds).containsExactly("p1", "p2")
            assertThat(s.selectionMode).isTrue()
            cancelAndIgnoreRemainingEvents()
        }

        vm.toggleSelection("p1")
        vm.state.test {
            var s = awaitItem()
            while (s.selectedIds.size != 1) s = awaitItem()
            assertThat(s.selectedIds).containsExactly("p2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun bulkActionsApplyToSelectionAndDeleteClearsIt() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(photo("p1", "a1")))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.toggleSelection("p1")

        vm.favoriteSelected(true)
        assertThat(photoRepo.lastFavorite).isEqualTo("p1" to true)

        vm.hideSelected(true)
        assertThat(photoRepo.lastInPlaylist).isEqualTo("p1" to false)

        vm.deleteSelected()
        assertThat(photoRepo.lastDeleted).isEqualTo("p1")
        vm.state.test {
            var s = awaitItem()
            while (s.selectedIds.isNotEmpty()) s = awaitItem()
            assertThat(s.selectionMode).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleHiddenShowsWhenAllSelectedAreHidden() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(
            photo("p1", "a1", inPlaylist = false),
            photo("p2", "a1", inPlaylist = false)
        ))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.toggleSelection("p1")
        vm.toggleSelection("p2")

        vm.toggleHiddenSelected()

        // every selected photo was hidden -> all are made visible (un-hidden)
        assertThat(photoRepo.inPlaylistCalls).containsExactly("p1" to true, "p2" to true)
    }

    @Test
    fun toggleHiddenHidesWhenAnySelectedIsVisible() = runTest {
        val photoRepo = FakePhotoRepository()
        photoRepo.photosByAlbum.value = mapOf("a1" to listOf(
            photo("p1", "a1", inPlaylist = false),
            photo("p2", "a1", inPlaylist = true)
        ))
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }
        vm.openAlbum("a1")
        vm.toggleSelection("p1")
        vm.toggleSelection("p2")

        vm.toggleHiddenSelected()

        // mixed selection (one visible) -> hide all
        assertThat(photoRepo.inPlaylistCalls).containsExactly("p1" to false, "p2" to false)
    }

    @Test
    fun reorderDelegatesToRepository() = runTest {
        val photoRepo = FakePhotoRepository()
        val vm = LibraryViewModel(FakeAlbumRepository(), photoRepo, FakeEntitlement(true), dispatcher) { 100L }

        vm.reorder(listOf("p3", "p1", "p2"))

        assertThat(photoRepo.lastReorder).containsExactly("p3", "p1", "p2").inOrder()
    }
}
