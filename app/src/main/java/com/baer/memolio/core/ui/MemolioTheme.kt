package com.baer.memolio.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MemolioColorScheme = darkColorScheme(
    primary = MemolioColors.Teal,
    onPrimary = MemolioColors.TextOnAccent,
    primaryContainer = MemolioColors.TealWash,
    onPrimaryContainer = MemolioColors.TealSoft,
    secondary = MemolioColors.Amber,
    onSecondary = MemolioColors.TextOnAccent,
    secondaryContainer = MemolioColors.AmberWash,
    onSecondaryContainer = MemolioColors.AmberSoft,
    background = MemolioColors.BgApp,
    onBackground = MemolioColors.TextPrimary,
    surface = MemolioColors.BgSurface,
    onSurface = MemolioColors.TextPrimary,
    surfaceVariant = MemolioColors.Ink200,
    onSurfaceVariant = MemolioColors.TextSecondary,
    outline = MemolioColors.BorderDefault,
    outlineVariant = MemolioColors.BorderSoft,
    error = MemolioColors.Error,
)

/**
 * Material 3 theme for the whole app. Always dark-leaning: the device is an
 * appliance on display 24/7, so a deep warm background minimizes glare and
 * burn-in regardless of system dark-mode setting. Color/typography/shape tokens
 * are the Memolio design system (Inter face + warm palette + soft radii).
 */
@Composable
fun MemolioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MemolioColorScheme,
        typography = MemolioTypography,
        shapes = MemolioShapes,
        content = content
    )
}
