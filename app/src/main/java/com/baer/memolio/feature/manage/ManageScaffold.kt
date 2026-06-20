package com.baer.memolio.feature.manage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.component.ButtonSize
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone
import com.baer.memolio.feature.manage.about.AboutScreen
import com.baer.memolio.feature.manage.addphotos.AddPhotosScreen
import com.baer.memolio.feature.manage.appliance.ApplianceScreen
import com.baer.memolio.feature.manage.library.LibraryScreen
import com.baer.memolio.feature.manage.playlist.PlaylistScreen
import com.baer.memolio.feature.manage.storage.StorageScreen
import com.baer.memolio.feature.manage.wallpaper.WallpaperScreen
import kotlinx.coroutines.launch

/**
 * Tablet-first list-detail (spec section 7): the design-system section rail in the
 * list pane, the selected section's UI in the detail pane. The adaptive navigator
 * still collapses to a single pane and back-navigates on compact widths, so the
 * fixed-looking rail stays adaptive per the project's "always adaptive" rule.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ManageScaffold(
    isPro: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ManageSection>()
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(ManageSection.default) }
    // Single-pane (portrait): the rail is hidden while a section is open, so the detail
    // pane needs its own back affordance; in two-pane (landscape) the rail is always
    // visible and this is false. Also routes the system back button to the list.
    val canBack = navigator.canNavigateBack()
    BackHandler(enabled = canBack) {
        scope.launch { navigator.navigateBack() }
    }

    ListDetailPaneScaffold(
        modifier = modifier.fillMaxSize(),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                ManageSectionRail(
                    selected = selected,
                    isPro = isPro,
                    onClose = onClose,
                    onOpenPaywall = onOpenPaywall,
                    onSelect = { section ->
                        selected = section
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, section)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                DetailPane(
                    showBack = canBack,
                    onBack = { scope.launch { navigator.navigateBack() } }
                ) {
                    when (selected) {
                        ManageSection.Library -> LibraryScreen(onOpenPaywall = onOpenPaywall)
                        ManageSection.Playlist -> PlaylistScreen(onOpenPaywall = onOpenPaywall)
                        ManageSection.AddPhotos -> AddPhotosScreen()
                        ManageSection.Appliance -> ApplianceScreen(isPro = isPro, onOpenPaywall = onOpenPaywall)
                        ManageSection.Storage -> StorageScreen()
                        ManageSection.Wallpaper -> WallpaperScreen(onOpenPaywall = onOpenPaywall)
                        ManageSection.About -> AboutScreen()
                    }
                }
            }
        }
    )
}

/**
 * The shared detail surface: deep app background, generously padded. Not itself
 * scrollable — sections own their scroll (Library/Storage use lazy/scroll roots),
 * which avoids nesting two vertical scrollers on the same axis.
 */
@Composable
private fun DetailPane(
    showBack: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    // Roomier 24dp in single-pane (portrait), the original 48dp in two-pane (landscape).
    val pad = if (showBack) 24.dp else 48.dp
    Column(
        Modifier
            .fillMaxSize()
            .background(MemolioColors.BgApp)
            .padding(pad)
    ) {
        if (showBack) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemolioIconButton(
                    icon = "arrow_back",
                    contentDescription = "Back to menu",
                    onClick = onBack,
                    variant = IconButtonVariant.Bare,
                    size = IconButtonSize.Md
                )
                Spacer(Modifier.width(8.dp))
                Text("Menu", color = MemolioColors.TextSecondary, style = MemolioType.body)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Left rail (design ManageApp): near-black surface, wordmark + back-to-frame, the
 * seven sections with icon + active glass pill + Pro lock, and an "Unlock Pro"
 * button pinned to the bottom for free users.
 */
@Composable
private fun ManageSectionRail(
    selected: ManageSection,
    isPro: Boolean,
    onClose: () -> Unit,
    onOpenPaywall: () -> Unit,
    onSelect: (ManageSection) -> Unit
) {
    // The list pane is a narrow 240dp rail in two-pane (landscape) but full-width when
    // single-pane (portrait). Enlarge the items where there's room; keep them compact in
    // the narrow rail. The list scrolls so all sections are reachable at any height.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val big = maxWidth > 360.dp
        Column(
            Modifier
                .fillMaxSize()
                .background(MemolioColors.Ink050)
                .padding(horizontal = 12.dp, vertical = 24.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemolioWordmark(tone = WordmarkTone.Solid, size = if (big) 22.sp else 18.sp)
                MemolioIconButton(
                    icon = "photo_camera_back",
                    contentDescription = "Back to frame",
                    onClick = onClose,
                    variant = IconButtonVariant.Bare,
                    size = IconButtonSize.Sm
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(if (big) 6.dp else 2.dp)
            ) {
                ManageSection.entries.forEach { section ->
                    RailItem(
                        section = section,
                        active = section == selected,
                        locked = section.pro && !isPro,
                        big = big,
                        onClick = { onSelect(section) }
                    )
                }
            }
            if (!isPro) {
                MemolioButton(
                    text = "Unlock Pro",
                    onClick = onOpenPaywall,
                    variant = ButtonVariant.Secondary,
                    size = ButtonSize.Sm,
                    icon = "auto_awesome",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RailItem(
    section: ManageSection,
    active: Boolean,
    locked: Boolean,
    big: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(if (big) 14.dp else 10.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (active) MemolioColors.SurfaceGlassStrong else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = if (big) 16.dp else 12.dp, vertical = if (big) 18.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Symbol(
            section.icon,
            size = if (big) 28.sp else 22.sp,
            tint = if (active) MemolioColors.Teal else MemolioColors.TextTertiary
        )
        Spacer(Modifier.width(if (big) 18.dp else 12.dp))
        Text(
            section.title,
            color = if (active) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
            style = if (big) MemolioType.bodyLg else MemolioType.body,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal
        )
        if (locked) {
            Spacer(Modifier.weight(1f))
            Symbol("lock", size = if (big) 18.sp else 15.sp, tint = MemolioColors.AmberSoft)
        }
    }
}
