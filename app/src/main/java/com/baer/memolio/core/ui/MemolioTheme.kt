package com.baer.memolio.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MemolioColorScheme = darkColorScheme(
    primary = MemolioAccentTeal,
    secondary = MemolioAccentAmber,
    background = MemolioBackground,
    surface = MemolioSurface,
    onPrimary = MemolioBackground,
    onBackground = MemolioInk,
    onSurface = MemolioInk
)

/**
 * Material 3 theme for the frame. Always dark-leaning: the device is an appliance
 * on display 24/7, so a deep background minimizes glare and burn-in regardless of
 * system dark-mode setting. Typography/shapes use the M3 defaults; overlay text
 * styling lives in the overlay composables (Task 4) since it is mockup-specific.
 */
@Composable
fun MemolioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MemolioColorScheme,
        content = content
    )
}
