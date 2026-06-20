package com.baer.memolio.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * M3 Typography so MaterialTheme-driven components (adaptive scaffold, etc.)
 * inherit Inter and the design scale. Maps the closest design tier to each role.
 *
 * Kept in its own file (not alongside [MemolioType]) to avoid a static-init cycle:
 * the [MemolioType] object references the file-level [InterFamily]/clock-shadow
 * vals, so co-locating this Typography (which reads [MemolioType] members) in the
 * same file facade would re-enter the half-initialized object.
 */
val MemolioTypography = Typography(
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
