package com.baer.memolio.feature.manage.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.model.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val allAlbums: List<Album> = emptyList(),
    val activeAlbumIds: Set<String> = emptySet(),
    val shuffle: Boolean = true,
    val intervalSeconds: Int = 30,
    val transition: TransitionStyle = TransitionStyle.KEN_BURNS_CROSSFADE,
    val fitMode: FitMode = FitMode.BLURRED_FILL,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showCaption: Boolean = true,
    val isPro: Boolean = false
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val albumRepository: AlbumRepository,
    private val entitlement: EntitlementRepository
) : ViewModel() {

    val state: StateFlow<PlaylistUiState> =
        combine(
            settings.playlistConfig,
            albumRepository.observeAlbums(),
            entitlement.isPro
        ) { config, albums, isPro ->
            PlaylistUiState(
                allAlbums = albums,
                activeAlbumIds = config.activeAlbumIds,
                shuffle = config.shuffle,
                intervalSeconds = config.intervalSeconds,
                transition = config.transition,
                fitMode = config.fitMode,
                showClock = config.showClock,
                showDate = config.showDate,
                showCaption = config.showCaption,
                isPro = isPro
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaylistUiState())

    fun toggleAlbum(albumId: String) = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        val current = settings.playlistConfig.first().activeAlbumIds
        settings.setActiveAlbumIds(
            if (albumId in current) current - albumId else current + albumId
        )
    }

    fun setShuffle(value: Boolean) = viewModelScope.launch { settings.setShuffle(value) }
    fun setInterval(seconds: Int) = viewModelScope.launch { settings.setIntervalSeconds(seconds) }
    fun setTransition(value: TransitionStyle) = viewModelScope.launch { settings.setTransition(value) }
    fun setFitMode(value: FitMode) = viewModelScope.launch { settings.setFitMode(value) }
    fun setShowClock(value: Boolean) = viewModelScope.launch { settings.setShowClock(value) }
    fun setShowDate(value: Boolean) = viewModelScope.launch { settings.setShowDate(value) }
    fun setShowCaption(value: Boolean) = viewModelScope.launch { settings.setShowCaption(value) }
}
