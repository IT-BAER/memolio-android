package com.baer.memolio.feature.manage

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone
import com.baer.memolio.feature.manage.about.AboutScreen
import com.baer.memolio.feature.manage.addphotos.AddPhotosScreen
import com.baer.memolio.feature.manage.appliance.ApplianceScreen
import com.baer.memolio.feature.manage.language.LanguageScreen
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
    // Single-pane (portrait): the rail is hidden while a section is open, so the detail pane
    // needs its own back affordance; in two-pane (landscape) the rail is always visible.
    // Derived from orientation (not navigator.canNavigateBack()) so the back row stays put
    // while the pane animates back — otherwise canNavigateBack() flips to false the instant
    // back is tapped and the header pops away before the slide. canBack still gates the
    // SYSTEM back button (only meaningful when there is a pane to pop).
    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
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
                    onClose = onClose,
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
                    showBack = portrait,
                    onBack = { scope.launch { navigator.navigateBack() } }
                ) {
                    // Modern page transition: the new section fades in and eases up from
                    // slightly below while the old one fades out. The back-row header above
                    // stays fixed, so only the content animates.
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            (fadeIn(tween(260, delayMillis = 40)) +
                                slideInHorizontally(tween(320)) { full -> full / 16 })
                                .togetherWith(fadeOut(tween(160)))
                        },
                        label = "manage-section"
                    ) { section ->
                        when (section) {
                            ManageSection.Library -> LibraryScreen(onOpenPaywall = onOpenPaywall)
                            ManageSection.Playlist -> PlaylistScreen(onOpenPaywall = onOpenPaywall)
                            ManageSection.AddPhotos -> AddPhotosScreen()
                            ManageSection.Appliance -> ApplianceScreen(isPro = isPro, onOpenPaywall = onOpenPaywall)
                            ManageSection.Storage -> StorageScreen()
                            ManageSection.Wallpaper -> WallpaperScreen(onOpenPaywall = onOpenPaywall)
                            ManageSection.Language -> LanguageScreen()
                            ManageSection.About -> AboutScreen()
                        }
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
    // Roomier 24dp in portrait, the original 48dp in landscape. Keyed off orientation (not
    // showBack) so the padding doesn't jump while the pane animates back to the menu.
    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val pad = if (portrait) 24.dp else 48.dp
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
                    contentDescription = stringResource(R.string.manage_back_to_menu),
                    onClick = onBack,
                    variant = IconButtonVariant.Bare,
                    size = IconButtonSize.Md
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.manage_menu), color = MemolioColors.TextSecondary, style = MemolioType.body)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Left rail (design ManageApp): near-black surface, wordmark + back-to-frame, and the
 * seven sections with icon + active glass pill. Pro features upsell from inside their
 * own pages, so the rail carries no lock badges or "Unlock Pro" button.
 */
@Composable
private fun ManageSectionRail(
    selected: ManageSection,
    onClose: () -> Unit,
    onSelect: (ManageSection) -> Unit
) {
    // The list pane is a narrow 240dp rail in two-pane (landscape) but full-width when
    // single-pane (portrait). Enlarge the items in portrait; keep them compact in the narrow
    // landscape rail. Keyed off orientation (NOT the pane's animating width) so the items
    // don't resize mid-transition when the pane slides back to the menu.
    val big = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
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
                contentDescription = stringResource(R.string.manage_back_to_frame),
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
                    big = big,
                    onClick = { onSelect(section) }
                )
            }
        }
    }
}

@Composable
private fun RailItem(
    section: ManageSection,
    active: Boolean,
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
            stringResource(section.titleRes),
            color = if (active) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
            style = if (big) MemolioType.bodyLg else MemolioType.body,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal
        )
    }
}
