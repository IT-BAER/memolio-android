package com.baer.memolio.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.baer.memolio.R

/**
 * The single source of truth for selectable wallpapers, shared by the picker (swatches),
 * the frame's idle home, and the Manage menu so a chosen wallpaper renders identically
 * everywhere. [brush] null means "render the live vector default" ([MemolioWallpaper]).
 */
data class WallpaperOption(val id: String, @StringRes val nameRes: Int, val brush: Brush?)

private val EmberBrush = Brush.linearGradient(
    0f to Color(0xFF0C0807), 0.55f to Color(0xFF1F1410), 1f to Color(0xFF2A1D14)
)
private val SlateBrush = Brush.linearGradient(
    0f to Color(0xFF07080C), 0.60f to Color(0xFF10151C), 1f to Color(0xFF161B22)
)

/** The built-in default (live vector wallpaper) plus the preset previews from the design. */
val MemolioWallpapers: List<WallpaperOption> = listOf(
    WallpaperOption("default", R.string.wallpaper_name_default, brush = null),
    WallpaperOption("ember", R.string.wallpaper_name_ember, EmberBrush),
    WallpaperOption("slate", R.string.wallpaper_name_slate, SlateBrush),
)

private fun brushFor(id: String): Brush? =
    MemolioWallpapers.firstOrNull { it.id == id }?.brush

/**
 * Renders the wallpaper for [wallpaperId] across the app. Unknown ids fall back to the
 * live default. [driftPhase]/[isPortrait] only affect the live vector default; the flat
 * preset gradients ignore them.
 */
@Composable
fun WallpaperBackground(
    wallpaperId: String,
    modifier: Modifier = Modifier,
    driftPhase: Float = 0f,
    isPortrait: Boolean = false,
) {
    val brush = brushFor(wallpaperId)
    if (brush == null) {
        MemolioWallpaper(modifier = modifier, driftPhase = driftPhase, isPortrait = isPortrait)
    } else {
        Box(modifier.fillMaxSize().background(brush))
    }
}
