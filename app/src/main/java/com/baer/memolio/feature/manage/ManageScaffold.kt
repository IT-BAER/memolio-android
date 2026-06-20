package com.baer.memolio.feature.manage

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.WallpaperBackground
import com.baer.memolio.feature.manage.wallpaper.WallpaperViewModel
import com.baer.memolio.core.ui.component.FramePlayButton
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone
import com.baer.memolio.feature.manage.addphotos.AddPhotosScreen
import com.baer.memolio.feature.manage.appliance.ApplianceScreen
import com.baer.memolio.feature.manage.library.LibraryScreen
import com.baer.memolio.feature.manage.playlist.PlaylistScreen
import com.baer.memolio.feature.manage.settings.SettingsScreen
import com.baer.memolio.feature.manage.storage.StorageScreen

/**
 * Manage is a card-grid launcher: a full-screen 2-column grid of section cards (icon +
 * label) is the landing; tapping a card pushes that section full-screen with a back row.
 * Works identically in portrait and landscape (the cards just get larger), so it stays
 * adaptive per the project's "always adaptive" rule. The bottom-right [FramePlayButton]
 * FAB returns to the playing frame from anywhere in Manage.
 */
@Composable
fun ManageScaffold(
    isPro: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier,
    wallpaperViewModel: WallpaperViewModel = hiltViewModel()
) {
    val wallpaperState by wallpaperViewModel.state.collectAsState()
    var openSection by remember { mutableStateOf<ManageSection?>(null) }
    // Back from an open section returns to the grid; back from the grid falls through to the
    // host (exit Manage → frame).
    BackHandler(enabled = openSection != null) { openSection = null }

    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    Box(modifier.fillMaxSize().background(MemolioColors.BgApp)) {
        // The selected wallpaper sits behind the (transparent) menu grid; pushed section
        // panes paint their own opaque background over it.
        WallpaperBackground(
            wallpaperId = wallpaperState.selectedId,
            modifier = Modifier.fillMaxSize(),
            isPortrait = portrait
        )
        AnimatedContent(
            targetState = openSection,
            transitionSpec = {
                val forward = targetState != null
                val dir = if (forward) 1 else -1
                (slideInHorizontally(tween(300)) { w -> dir * w / 8 } + fadeIn(tween(220)))
                    .togetherWith(slideOutHorizontally(tween(300)) { w -> -dir * w / 8 } + fadeOut(tween(160)))
            },
            label = "manage-root"
        ) { section ->
            if (section == null) {
                ManageMenuGrid(onSelect = { openSection = it })
            } else {
                ManageSectionDetail(
                    section = section,
                    isPro = isPro,
                    onOpenPaywall = onOpenPaywall,
                    onBack = { openSection = null }
                )
            }
        }

        // Floating "play the frame" action: bottom-right FAB, present in both orientations.
        FramePlayButton(
            onClick = onClose,
            contentDescription = stringResource(R.string.manage_back_to_frame),
            size = if (portrait) 64.dp else 60.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        )
    }
}

/**
 * The landing grid: wordmark header + a 2-column grid of section cards. The grid scrolls;
 * extra bottom padding keeps the last row clear of the floating play FAB.
 */
@Composable
private fun ManageMenuGrid(onSelect: (ManageSection) -> Unit) {
    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val edge = if (portrait) 24.dp else 48.dp
    Column(Modifier.fillMaxSize().padding(horizontal = edge)) {
        MemolioWordmark(
            tone = WordmarkTone.Solid,
            size = if (portrait) 24.sp else 22.sp,
            modifier = Modifier.padding(top = 28.dp, bottom = 22.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            contentPadding = PaddingValues(bottom = 112.dp)
        ) {
            items(ManageSection.entries) { section ->
                MenuCard(section = section, onClick = { onSelect(section) })
            }
        }
    }
}

/** A single section card: icon centered, label below. The accent shows in the icon only. */
@Composable
private fun MenuCard(section: ManageSection, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .height(108.dp)
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(MemolioColors.SurfaceCard)
            .border(1.dp, MemolioColors.BorderDefault, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Symbol(section.icon, size = 34.sp, tint = MemolioColors.Teal)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(section.titleRes),
            color = MemolioColors.TextPrimary,
            style = MemolioType.body,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A pushed section: back-to-menu row above the section's own UI. Roomier 24dp padding in
 * portrait, the original 48dp in landscape.
 */
@Composable
private fun ManageSectionDetail(
    section: ManageSection,
    isPro: Boolean,
    onOpenPaywall: () -> Unit,
    onBack: () -> Unit
) {
    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val pad = if (portrait) 24.dp else 48.dp
    Column(
        Modifier
            .fillMaxSize()
            // Light scrim only — the selected wallpaper (drawn behind AnimatedContent at the
            // scaffold root) stays visible through every section, matching the menu grid + frame.
            // Cards/inputs carry their own opaque-enough fills, so content stays legible.
            .background(MemolioColors.BgApp.copy(alpha = 0.35f))
            .padding(pad)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemolioIconButton(
                icon = "arrow_back",
                contentDescription = stringResource(R.string.manage_back_to_menu),
                onClick = onBack,
                variant = IconButtonVariant.Bare,
                size = IconButtonSize.Md
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(section.titleRes),
                color = MemolioColors.TextPrimary,
                style = MemolioType.h2
            )
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (section) {
                ManageSection.Library -> LibraryScreen(onOpenPaywall = onOpenPaywall)
                ManageSection.Playlist -> PlaylistScreen(onOpenPaywall = onOpenPaywall)
                ManageSection.AddPhotos -> AddPhotosScreen()
                ManageSection.Appliance -> ApplianceScreen(isPro = isPro, onOpenPaywall = onOpenPaywall)
                ManageSection.Storage -> StorageScreen()
                ManageSection.Settings -> SettingsScreen(onOpenPaywall = onOpenPaywall)
            }
        }
    }
}
