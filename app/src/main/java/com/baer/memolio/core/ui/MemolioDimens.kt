package com.baer.memolio.core.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Spacing, radii, hairline widths and motion tokens, ported from the design
 * system's spacing/radii/motion `:root` block. The frame is calm and roomy:
 * generous spacing, soft circular buttons, hairline borders.
 */
object MemolioSpacing {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 24.dp
    val s6 = 32.dp
    val s7 = 48.dp
    val s8 = 64.dp
    val s9 = 96.dp // frame overlay margins
    val s10 = 132.dp // clock bottom inset
}

/** Corner radii. Cards gently rounded; buttons & glass chips fully round. */
object MemolioRadii {
    val xs = RoundedCornerShape(6.dp)
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(14.dp) // cards
    val lg = RoundedCornerShape(20.dp)
    val xl = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(percent = 50) // circular glass buttons
}

/** Hairline border widths. */
object MemolioBorders {
    val hair = 1.dp
    val thick = 2.dp
}

/**
 * Motion: slow, cinematic. Crossfades ~1.2s, Ken Burns 20s. UI interactions are
 * gentle eases; no bounces. Durations are millis.
 */
object MemolioMotion {
    val EaseStandard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
    val EaseOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    const val DurFast = 150
    const val DurBase = 240
    const val DurSlow = 1200
    const val DurKenBurns = 20_000
}
