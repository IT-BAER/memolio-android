package com.baer.memolio.core.ui

import androidx.compose.ui.graphics.Color

/**
 * Shared overlay color tokens, ported from the mockup CSS custom properties
 * (`--ink`, `--muted`, `--faint`, `--glass` in docs/mockups/home-wallpaper.html).
 * These are used directly by the overlay composables so text legibility is
 * identical in both Frame states (idle and slideshow).
 */

/** `--ink`: #f4f1ea — warm near-white for the clock and primary text. */
val MemolioInk = Color(0xFFF4F1EA)

/** `--muted`: ink at 68% — secondary text (date). */
val MemolioMuted = Color(0xFFF4F1EA).copy(alpha = 0.68f)

/** `--faint`: ink at 28%; the wordmark uses 34% (see CSS). */
val MemolioFaint = Color(0xFFF4F1EA).copy(alpha = 0.28f)
val MemolioWordmarkColor = Color(0xFFF4F1EA).copy(alpha = 0.34f)

/** `--glass`: subtle translucent panel fill for the menu button. */
val MemolioGlass = Color(0xFFFFFFFF).copy(alpha = 0.035f)
val MemolioGlassStroke = Color(0xFFFFFFFF).copy(alpha = 0.10f)

/** Deep frame background, the darkest stop of the mockup base gradient (#07080c). */
val MemolioBackground = Color(0xFF07080C)
val MemolioSurface = Color(0xFF10151C)

/** Ambient accents pulled from the mockup's teal + amber panels. */
val MemolioAccentTeal = Color(0xFF5BA49A)
val MemolioAccentAmber = Color(0xFFA68062)
