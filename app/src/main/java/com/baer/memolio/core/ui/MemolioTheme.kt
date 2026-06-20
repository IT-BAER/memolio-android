package com.baer.memolio.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

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
    // Pick the UI font family for the active locale (system font for CJK, else Inter) before
    // building the typography, so both MemolioType getters and M3 components render glyph-safe.
    val locale = LocalConfiguration.current.locales[0]
    activeUiFontFamily = uiFontFamilyFor(locale.language)
    MaterialTheme(
        colorScheme = MemolioColorScheme,
        typography = memolioTypography(),
        shapes = MemolioShapes,
        content = content
    )
}
