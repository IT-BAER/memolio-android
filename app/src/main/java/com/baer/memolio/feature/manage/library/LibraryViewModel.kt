package com.baer.memolio.feature.manage.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val albums: List<Album> = emptyList(),
    /** albumId -> cover thumbnail path (the album's first live photo). */
    val albumCovers: Map<String, String> = emptyMap(),
    val openAlbumId: String? = null,
    val openAlbumPhotos: List<Photo> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    /** Free "All photos" pool view: the whole live pool, with per-photo slideshow toggles. */
    val showAllPhotos: Boolean = false,
    val allPhotos: List<Photo> = emptyList(),
    val isPro: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val photoRepository: PhotoRepository,
    private val entitlement: EntitlementRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    // Secondary constructor for tests: allows injecting a clock stub.
    internal constructor(
        albumRepository: AlbumRepository,
        photoRepository: PhotoRepository,
        entitlement: EntitlementRepository,
        ioDispatcher: CoroutineDispatcher,
        now: () -> Long
    ) : this(albumRepository, photoRepository, entitlement, ioDispatcher) {
        this.nowFn = now
    }

    private var nowFn: () -> Long = { System.currentTimeMillis() }

    private val openAlbumId = MutableStateFlow<String?>(null)
    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val showAllPhotos = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val openPhotos = openAlbumId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList()) else photoRepository.observePhotos(id)
    }

    // The whole live pool (including photos hidden from the slideshow) for the All-photos view.
    private val allPhotos: Flow<List<Photo>> = photoRepository.observeAllLivePhotos()

    // albumId -> the first live photo's thumbnail, used as each album card's cover. Derived
    // from the whole live pool in one query (no per-album subscription).
    private val albumCovers: Flow<Map<String, String>> =
        photoRepository.observeAllLivePhotos().map { photos ->
            photos.groupBy { it.albumId }.mapValues { (_, list) -> list.first().thumbPath }
        }

    val state: StateFlow<LibraryUiState> =
        combine(
            combine(albumRepository.observeAlbums(), albumCovers, allPhotos) { albums, covers, all ->
                Triple(albums, covers, all)
            },
            combine(openAlbumId, showAllPhotos) { openId, showAll -> openId to showAll },
            openPhotos,
            selectedIds,
            entitlement.isPro
        ) { (albums, covers, all), (openId, showAll), photos, selected, isPro ->
            LibraryUiState(
                albums = albums,
                albumCovers = covers,
                allPhotos = all,
                openAlbumId = openId,
                showAllPhotos = showAll,
                openAlbumPhotos = photos,
                selectedIds = selected,
                isPro = isPro
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun createAlbum(name: String) = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        val ts = nowFn()
        albumRepository.upsert(
            Album(id = "alb_$ts", name = name, coverPhotoId = null, createdAt = ts, sortOrder = 0)
        )
    }

    fun renameAlbum(album: Album, newName: String) = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        albumRepository.upsert(album.copy(name = newName))
    }

    fun deleteAlbum(id: String) = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        albumRepository.delete(id)
    }

    fun openAlbum(id: String) {
        // Blank id is treated as "no album open" so the detail view never queries an
        // empty album (which would silently show zero photos). See closeAlbum.
        openAlbumId.value = id.ifBlank { null }
        selectedIds.value = emptySet()
    }

    /** Return from album detail to the albums list. Back button + system back both call this. */
    fun closeAlbum() {
        openAlbumId.value = null
        selectedIds.value = emptySet()
    }

    /** Open/close the free "All photos" pool view (per-photo slideshow include/exclude). */
    fun openAllPhotos() {
        showAllPhotos.value = true
        selectedIds.value = emptySet()
    }

    fun closeAllPhotos() {
        showAllPhotos.value = false
    }

    fun setInPlaylist(photoId: String, inPlaylist: Boolean) = viewModelScope.launch {
        photoRepository.setInPlaylist(photoId, inPlaylist)
    }

    fun toggleSelection(photoId: String) = selectedIds.update { current ->
        if (photoId in current) current - photoId else current + photoId
    }

    fun moveSelectedTo(albumId: String) = viewModelScope.launch {
        if (!entitlement.isPro.first()) { selectedIds.value = emptySet(); return@launch }
        selectedIds.value.forEach { photoRepository.moveToAlbum(it, albumId) }
    }

    fun favoriteSelected(favorite: Boolean) = viewModelScope.launch {
        selectedIds.value.forEach { photoRepository.setFavorite(it, favorite) }
    }

    fun deleteSelected() = viewModelScope.launch {
        val ts = nowFn()
        selectedIds.value.forEach { photoRepository.softDelete(it, ts) }
        selectedIds.value = emptySet()
    }

    fun reorder(orderedIds: List<String>) = viewModelScope.launch {
        photoRepository.reorder(orderedIds)
    }
}
