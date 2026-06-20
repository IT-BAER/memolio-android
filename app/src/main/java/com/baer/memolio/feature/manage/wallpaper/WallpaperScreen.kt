package com.baer.memolio.feature.manage.wallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.MemolioWallpaper
import com.baer.memolio.core.ui.component.ProLock
import com.baer.memolio.core.ui.component.SectionHead

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: WallpaperViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier.verticalScroll(rememberScrollState())) {
        SectionHead(title = stringResource(R.string.wallpaper_title), sub = stringResource(R.string.wallpaper_subtitle))
        if (!state.isPro) {
            ProLock(
                feature = stringResource(R.string.profeature_wallpaper_lock),
                onUpsell = onOpenPaywall,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
        // Wraps to the next line when the pane is narrow (portrait) instead of clipping.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WALLPAPERS.forEach { w ->
                Swatch(
                    spec = w,
                    selected = w.id == state.selectedId,
                    onClick = { viewModel.select(w.id) }
                )
            }
        }
    }
}

/** The built-in default (live wallpaper) plus the two preset previews from the design. */
private data class WallpaperSpec(val id: String, @StringRes val nameRes: Int, val brush: Brush?)

private val WALLPAPERS = listOf(
    WallpaperSpec("default", R.string.wallpaper_name_default, brush = null), // null = render the live vector wallpaper
    WallpaperSpec(
        "ember", R.string.wallpaper_name_ember,
        Brush.linearGradient(0f to Color(0xFF0C0807), 0.55f to Color(0xFF1F1410), 1f to Color(0xFF2A1D14))
    ),
    WallpaperSpec(
        "slate", R.string.wallpaper_name_slate,
        Brush.linearGradient(0f to Color(0xFF07080C), 0.60f to Color(0xFF10151C), 1f to Color(0xFF161B22))
    ),
)

@Composable
private fun Swatch(spec: WallpaperSpec, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier
                .size(width = 200.dp, height = 124.dp)
                .clip(shape)
                .then(if (spec.brush != null) Modifier.background(spec.brush) else Modifier)
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MemolioColors.Teal else MemolioColors.BorderDefault,
                    shape
                )
        ) {
            if (spec.brush == null) MemolioWallpaper(Modifier.size(width = 200.dp, height = 124.dp))
        }
        Text(
            text = stringResource(spec.nameRes),
            color = if (selected) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
            style = MemolioType.sm,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
