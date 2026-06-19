package com.baer.memolio.feature.manage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.ui.Modifier
import com.baer.memolio.feature.manage.about.AboutScreen
import com.baer.memolio.feature.manage.addphotos.AddPhotosScreen
import com.baer.memolio.feature.manage.library.LibraryScreen
import com.baer.memolio.feature.manage.playlist.PlaylistScreen
import com.baer.memolio.feature.manage.storage.StorageScreen
import com.baer.memolio.feature.manage.wallpaper.WallpaperScreen
import kotlinx.coroutines.launch

/**
 * Tablet-first list-detail (spec section 7): section rail in the list pane, the selected
 * section's UI in the detail pane. On compact widths the adaptive navigator collapses to a
 * single pane and back-navigates from detail to list automatically.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ManageScaffold(
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ManageSection>()
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(ManageSection.default) }

    ListDetailPaneScaffold(
        modifier = modifier.fillMaxSize(),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                ManageSectionRail(
                    selected = selected,
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
                when (selected) {
                    ManageSection.Library -> LibraryScreen()
                    ManageSection.Playlist -> PlaylistScreen()
                    ManageSection.AddPhotos -> AddPhotosScreen()
                    ManageSection.Appliance -> ApplianceScreen()
                    ManageSection.Storage -> StorageScreen()
                    ManageSection.Wallpaper -> WallpaperScreen()
                    ManageSection.About -> AboutScreen()
                }
            }
        }
    )
}

/** Left rail listing the seven sections. Kept simple (a clickable column of labels). */
@Composable
private fun ManageSectionRail(
    selected: ManageSection,
    onSelect: (ManageSection) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ManageSection.entries.forEach { section ->
            NavigationRailItem(
                selected = section == selected,
                onClick = { onSelect(section) },
                icon = {},
                label = { Text(section.title) }
            )
        }
    }
}

/**
 * Appliance section: its toggles (kiosk, set-as-Home, autostart, sleep schedule, ambient
 * dimming) are pure SettingsRepository writes whose BEHAVIORS land in Phase 5. Rendered as a
 * thin placeholder here; the bound switches + behaviors are wired in Phase 5.
 */
@Composable
private fun ApplianceScreen() {
    Text("Appliance settings (toggles bound in Phase 5)")
}
