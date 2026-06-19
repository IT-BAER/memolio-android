package com.baer.memolio.feature.manage.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperUiState(
    val available: List<String> = listOf("default"),
    val selectedId: String = "default"
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    // v1 ships only the built-in default (custom wallpapers are a future feature).
    private val builtIn = listOf("default")

    val state: StateFlow<WallpaperUiState> =
        settings.appSettings
            .map { WallpaperUiState(available = builtIn, selectedId = it.wallpaperId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WallpaperUiState())

    fun select(id: String) = viewModelScope.launch { settings.setWallpaperId(id) }
}
