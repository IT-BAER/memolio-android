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

/**
 * The default "All photos" album id. This is a real Room album (created in MemolioApp,
 * the default bucket for web uploads — see FrameServer's DEFAULT_ALBUM_ID), but the
 * library treats it specially: its tile shows the WHOLE live pool (every album's photos),
 * and it is pulled out of the normal album list so it renders exactly once. Free; the
 * user-created albums are Pro.
 */
const val ALL_PHOTOS_ID = "all"

data class LibraryUiState(
    val albums: List<Album> = emptyList(),
    /** albumId -> cover thumbnail path (the album's first live photo). */
    val albumCovers: Map<String, String> = emptyMap(),
    /** The whole live pool — backs the "All photos" virtual album (cover, count, contents). */
    val allPhotos: List<Photo> = emptyList(),
    /** [ALL_PHOTOS_ID], a real album id, or null when showing the album list. */
    val openAlbumId: String? = null,
    val openAlbumPhotos: List<Photo> = emptyList(),
    /** Photo currently shown full-screen in the preview overlay; null = grid. */
    val previewPhotoId: String? = null,
    /** Long-press multi-select: ids checked in the open detail. Non-empty = selection mode. */
    val selectedIds: Set<String> = emptySet(),
    val isPro: Boolean = false
) {
    val isAllPhotosOpen: Boolean get() = openAlbumId == ALL_PHOTOS_ID
    val selectionMode: Boolean get() = selectedIds.isNotEmpty()
    /** The previewed photo resolved from the open detail, kept live for favorite/hidden toggles. */
    val previewPhoto: Photo? get() = previewPhotoId?.let { id -> openAlbumPhotos.firstOrNull { it.id == id } }
}

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
    private val previewPhotoId = MutableStateFlow<String?>(null)
    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    // The whole live pool (including photos hidden from the slideshow).
    private val allPhotos: Flow<List<Photo>> = photoRepository.observeAllLivePhotos()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val openPhotos = openAlbumId.flatMapLatest { id ->
        when {
            id.isNullOrBlank() -> flowOf(emptyList())
            id == ALL_PHOTOS_ID -> photoRepository.observeAllLivePhotos()
            else -> photoRepository.observePhotos(id)
        }
    }

    // albumId -> the first live photo's thumbnail, used as each album card's cover. Derived
    // from the whole live pool in one query (no per-album subscription).
    private val albumCovers: Flow<Map<String, String>> =
        allPhotos.map { photos ->
            photos.groupBy { it.albumId }.mapValues { (_, list) -> list.first().thumbPath }
        }

    val state: StateFlow<LibraryUiState> =
        combine(
            combine(albumRepository.observeAlbums(), albumCovers, allPhotos) { albums, covers, all ->
                Triple(albums, covers, all)
            },
            openAlbumId,
            openPhotos,
            combine(previewPhotoId, selectedIds) { previewId, selected -> previewId to selected },
            entitlement.isPro
        ) { (albums, covers, all), openId, photos, (previewId, selected), isPro ->
            LibraryUiState(
                albums = albums,
                albumCovers = covers,
                allPhotos = all,
                openAlbumId = openId,
                openAlbumPhotos = photos,
                previewPhotoId = previewId,
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

    /**
     * Delete the currently open album, keeping its photos. The album-delete FK cascades to
     * its photos, so we first reassign them to the default [ALL_PHOTOS_ID] bucket; the album
     * is then empty and safe to remove. The "All photos" bucket itself can never be deleted.
     */
    fun deleteOpenAlbum() = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        val id = openAlbumId.value
        if (id.isNullOrBlank() || id == ALL_PHOTOS_ID) return@launch
        photoRepository.observePhotos(id).first().forEach {
            photoRepository.moveToAlbum(it.id, ALL_PHOTOS_ID)
        }
        albumRepository.delete(id)
        closeAlbum()
    }

    /** Open a real album, or [ALL_PHOTOS_ID] for the whole pool. Blank id = album list. */
    fun openAlbum(id: String) {
        openAlbumId.value = id.ifBlank { null }
        previewPhotoId.value = null
        selectedIds.value = emptySet()
    }

    /** Open the free "All photos" virtual album (whole live pool). */
    fun openAllPhotos() = openAlbum(ALL_PHOTOS_ID)

    /** Return from a detail view to the album list. Back button + system back both call this. */
    fun closeAlbum() {
        openAlbumId.value = null
        previewPhotoId.value = null
        selectedIds.value = emptySet()
    }

    /** Full-screen preview of a single photo within the open detail. */
    fun openPreview(photoId: String) { previewPhotoId.value = photoId }
    fun closePreview() { previewPhotoId.value = null }

    /**
     * Reset the navigation sub-state to the album list. Called when the Library section is
     * (re)entered, because this ViewModel is activity-scoped and would otherwise reopen the
     * album/preview the user last left.
     */
    fun resetView() {
        openAlbumId.value = null
        previewPhotoId.value = null
        selectedIds.value = emptySet()
    }

    // ---- Long-press multi-select ----

    /** Toggle a photo's membership in the selection (long-press to start, tap to add/remove). */
    fun toggleSelection(photoId: String) = selectedIds.update { cur ->
        if (photoId in cur) cur - photoId else cur + photoId
    }

    fun clearSelection() { selectedIds.value = emptySet() }

    fun favoriteSelected(favorite: Boolean) = viewModelScope.launch {
        selectedIds.value.forEach { photoRepository.setFavorite(it, favorite) }
    }

    fun hideSelected(hidden: Boolean) = viewModelScope.launch {
        selectedIds.value.forEach { photoRepository.setInPlaylist(it, !hidden) }
    }

    fun deleteSelected() = viewModelScope.launch {
        val ts = nowFn()
        selectedIds.value.forEach { photoRepository.softDelete(it, ts) }
        selectedIds.value = emptySet()
    }

    fun setInPlaylist(photoId: String, inPlaylist: Boolean) = viewModelScope.launch {
        photoRepository.setInPlaylist(photoId, inPlaylist)
    }

    fun favorite(photoId: String, favorite: Boolean) = viewModelScope.launch {
        photoRepository.setFavorite(photoId, favorite)
    }

    /** Soft-delete a single photo and close the preview (the photo leaves the grid). */
    fun deletePhoto(photoId: String) = viewModelScope.launch {
        photoRepository.softDelete(photoId, nowFn())
        if (previewPhotoId.value == photoId) previewPhotoId.value = null
    }

    fun moveToAlbum(photoId: String, albumId: String) = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        photoRepository.moveToAlbum(photoId, albumId)
    }

    fun reorder(orderedIds: List<String>) = viewModelScope.launch {
        photoRepository.reorder(orderedIds)
    }
}
