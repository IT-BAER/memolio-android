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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val albums: List<Album> = emptyList(),
    val openAlbumId: String? = null,
    val openAlbumPhotos: List<Photo> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private val openPhotos = openAlbumId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else photoRepository.observePhotos(id)
    }

    val state: StateFlow<LibraryUiState> =
        combine(
            albumRepository.observeAlbums(),
            openAlbumId,
            openPhotos,
            selectedIds,
            entitlement.isPro
        ) { albums, openId, photos, selected, isPro ->
            LibraryUiState(
                albums = albums,
                openAlbumId = openId,
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
        openAlbumId.value = id
        selectedIds.value = emptySet()
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
