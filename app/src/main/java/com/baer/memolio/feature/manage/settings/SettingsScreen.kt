package com.baer.memolio.feature.manage.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baer.memolio.feature.manage.about.AboutScreen
import com.baer.memolio.feature.manage.language.LanguageScreen
import com.baer.memolio.feature.manage.wallpaper.WallpaperScreen

/**
 * Settings is a single scroll of distinct, well-spaced groups (Wallpaper, Language, About).
 * Each sub-screen renders its own [com.baer.memolio.core.ui.component.SectionHead]; the 40dp
 * gap between them is what stops the page from reading as one stuffed list.
 *
 * The trailing [Spacer] (not a bottom-padding modifier) is the clearance that keeps the last
 * card off the floating play FAB: a Spacer is real scroll content, so the card can scroll
 * fully clear of it. A `padding(bottom=…)` on the scroll modifier instead shrinks the viewport
 * and leaves a dead margin, clipping the last card short of the visible bottom.
 */
@Composable
fun SettingsScreen(
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        WallpaperScreen(onOpenPaywall = onOpenPaywall, scrollable = false)
        LanguageScreen(scrollable = false)
        AboutScreen()
        // Small breathing room only — the last card is left-aligned and the play FAB is
        // bottom-right, so no large clearance is needed here. A big spacer would force the
        // page to overflow and clip the card.
        Spacer(Modifier.height(16.dp))
    }
}
