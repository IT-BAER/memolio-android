package com.baer.memolio.feature.manage.wallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.MemolioWallpaper
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.WallpaperOption
import com.baer.memolio.core.ui.MemolioWallpapers
import com.baer.memolio.core.ui.component.SectionHead

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: WallpaperViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    scrollable: Boolean = true
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    // Smaller previews in portrait so the (taller, narrower) settings page fits without
    // scrolling; the roomier landscape size is kept for the wide list-detail layout.
    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val swatchW = if (portrait) 152.dp else 200.dp
    val swatchH = if (portrait) 94.dp else 124.dp
    Column(modifier.then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier)) {
        SectionHead(title = stringResource(R.string.wallpaper_title), sub = stringResource(R.string.wallpaper_subtitle))
        // Wraps to the next line when the pane is narrow (portrait) instead of clipping.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MemolioWallpapers.forEach { w ->
                // All built-in wallpapers are free; only future custom uploads are Pro-gated.
                Swatch(
                    spec = w,
                    selected = w.id == state.selectedId,
                    locked = false,
                    width = swatchW,
                    height = swatchH,
                    onClick = { viewModel.select(w.id) }
                )
            }
        }
    }
}

@Composable
private fun Swatch(spec: WallpaperOption, selected: Boolean, locked: Boolean, width: Dp, height: Dp, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier
                .size(width = width, height = height)
                .clip(shape)
                .then(if (spec.brush != null) Modifier.background(spec.brush) else Modifier)
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MemolioColors.Teal else MemolioColors.BorderDefault,
                    shape
                )
        ) {
            if (spec.brush == null) MemolioWallpaper(Modifier.size(width = width, height = height))
            if (locked) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MemolioColors.Ink000.copy(alpha = 0.55f))
                        .padding(6.dp)
                ) {
                    Symbol("lock", size = 18.sp, tint = MemolioColors.Paper)
                }
            }
        }
        Text(
            text = stringResource(spec.nameRes),
            color = if (selected) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
            style = MemolioType.sm,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
