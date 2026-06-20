package com.baer.memolio.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * M3 Typography so MaterialTheme-driven components (adaptive scaffold, etc.) inherit Inter
 * and the design scale. Maps the closest design tier to each role.
 *
 * A function (not a val) so it is rebuilt per composition by [MemolioTheme] AFTER it sets
 * [activeUiFontFamily] for the active locale: the text-tier members of [MemolioType] are
 * getters, so building the Typography here captures the right family (Inter, or the system
 * font for CJK). Being a function also sidesteps the original static-init cycle.
 */
fun memolioTypography(): Typography = Typography(
    displayLarge = MemolioType.display,
    headlineLarge = MemolioType.h1,
    headlineMedium = MemolioType.h2,
    titleLarge = MemolioType.h3,
    titleMedium = MemolioType.bodyLg.copy(fontWeight = FontWeight.Medium),
    bodyLarge = MemolioType.body,
    bodyMedium = MemolioType.sm,
    bodySmall = MemolioType.xs,
    labelLarge = MemolioType.sm.copy(fontWeight = FontWeight.Medium),
    labelMedium = MemolioType.label,
    labelSmall = MemolioType.label,
)
