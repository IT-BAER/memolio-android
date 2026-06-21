package com.baer.memolio.feature.manage.wallpaper

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.MemolioWallpaper
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.WallpaperOption
import com.baer.memolio.core.ui.MemolioWallpapers
import com.baer.memolio.core.ui.CUSTOM_WALLPAPER_ID
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

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.pickCustom(uri) }

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

            // Custom wallpaper slot (Pro-only).
            if (state.isPro) {
                CustomSwatch(
                    customPath = state.customPath,
                    selected = state.selectedId == CUSTOM_WALLPAPER_ID,
                    width = swatchW,
                    height = swatchH,
                    onSelect = { viewModel.select(CUSTOM_WALLPAPER_ID) },
                    onPick = {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            } else {
                // Locked swatch: tapping opens the paywall.
                Swatch(
                    spec = WallpaperOption(CUSTOM_WALLPAPER_ID, R.string.wallpaper_custom, brush = null),
                    selected = false,
                    locked = true,
                    width = swatchW,
                    height = swatchH,
                    onClick = onOpenPaywall
                )
            }
        }
    }
}

/**
 * Custom wallpaper swatch for Pro users.
 *
 * Interaction design (v1):
 * - When no custom image has been imported yet: tapping launches the photo picker.
 * - When an image is already imported: tapping selects it (makes it the active wallpaper).
 *   A "Choose a photo" text button below the swatch lets the user replace it by re-picking.
 */
@Composable
private fun CustomSwatch(
    customPath: String?,
    selected: Boolean,
    width: Dp,
    height: Dp,
    onSelect: () -> Unit,
    onPick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val hasImage = customPath != null

    Column(
        Modifier.clickable(onClick = if (hasImage) onSelect else onPick)
    ) {
        Box(
            Modifier
                .size(width = width, height = height)
                .clip(shape)
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MemolioColors.Teal else MemolioColors.BorderDefault,
                    shape
                )
        ) {
            if (hasImage) {
                AsyncImage(
                    model = java.io.File(customPath!!),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder: dark background + centered "+" icon.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MemolioColors.Ink000),
                    contentAlignment = Alignment.Center
                ) {
                    Symbol("add", size = 28.sp, tint = MemolioColors.TextSecondary)
                }
            }
        }

        Text(
            text = stringResource(R.string.wallpaper_custom),
            color = if (selected) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
            style = MemolioType.sm,
            modifier = Modifier.padding(top = 8.dp)
        )

        // "Choose a photo" appears below the label when an image exists, to allow re-picking.
        if (hasImage) {
            TextButton(
                onClick = onPick,
                modifier = Modifier.padding(top = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.wallpaper_custom_choose),
                    color = MemolioColors.Teal,
                    style = MemolioType.sm
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
