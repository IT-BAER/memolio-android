package com.baer.memolio.feature.manage.wallpaper

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.data.WallpaperRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.ui.CUSTOM_WALLPAPER_ID
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
    val isPro: Boolean = false,
    val customPath: String? = null
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val entitlement: EntitlementRepository,
    private val wallpaperRepo: WallpaperRepository
) : ViewModel() {

    // Built-in wallpapers are free; only custom uploads require Pro.
    private val builtIn = listOf("default", "ember", "slate")

    val state: StateFlow<WallpaperUiState> =
        combine(settings.appSettings, entitlement.isPro) { s, isPro ->
            WallpaperUiState(
                available = builtIn,
                selectedId = s.wallpaperId,
                isPro = isPro,
                customPath = wallpaperRepo.customWallpaperPath()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WallpaperUiState())

    fun select(id: String) = viewModelScope.launch {
        val isCustom = id == CUSTOM_WALLPAPER_ID
        if (!builtIn.contains(id)) {
            if (!entitlement.isPro.first()) return@launch
            if (isCustom && wallpaperRepo.customWallpaperPath() == null) return@launch
        }
        settings.setWallpaperId(id)
    }

    fun pickCustom(uri: Uri) = viewModelScope.launch {
        if (!entitlement.isPro.first()) return@launch
        wallpaperRepo.importCustom(uri)
        settings.setWallpaperId(CUSTOM_WALLPAPER_ID)
    }
}
