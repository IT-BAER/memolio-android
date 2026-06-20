package com.baer.memolio.feature.manage.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperUiState(
    val available: List<String> = listOf("default"),
    val selectedId: String = "default",
    val isPro: Boolean = false
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val entitlement: EntitlementRepository
) : ViewModel() {

    // Built-in wallpapers are free; only future custom uploads require Pro.
    private val builtIn = listOf("default", "ember", "slate")

    val state: StateFlow<WallpaperUiState> =
        combine(settings.appSettings, entitlement.isPro) { s, isPro ->
            WallpaperUiState(available = builtIn, selectedId = s.wallpaperId, isPro = isPro)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WallpaperUiState())

    fun select(id: String) = viewModelScope.launch {
        if (!builtIn.contains(id) && !entitlement.isPro.first()) return@launch
        settings.setWallpaperId(id)
    }
}
