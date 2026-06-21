package com.baer.memolio.core.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Orientation-aware frame overlay metrics, derived off the SHORT edge (vmin) so the
 * single overlay composition re-flows identically in portrait (the primary
 * orientation) and landscape (secondary) — no separate layout code.
 *
 * Formulas are the design system's `tokens/spacing.css` clamp() rules
 * (vmin = min(width, height)):
 * ```
 *   --frame-inset-x:      clamp(24px, 6vmin, 64px)
 *   --frame-inset-top:    clamp(22px, 5vmin, 52px)
 *   --frame-inset-bottom: clamp(48px, 10vmin, 120px)
 *   --frame-clock-size:   clamp(88px, 26vmin, 260px)
 *   --frame-date-size:    clamp(20px, 3.4vmin, 40px)
 * ```
 * Wordmark, caption and the date rule are sized off the same short edge.
 */
data class FrameMetrics(
    val isPortrait: Boolean,
    val insetX: Dp,
    val insetTop: Dp,
    val insetBottom: Dp,
    val clockSize: TextUnit,
    val clockDiameter: Dp,
    val dateSize: TextUnit,
    val wordmarkSize: TextUnit,
    val captionSize: TextUnit,
    val ruleWidth: Dp,
) {
    companion object {
        /**
         * Fallback ≈ landscape-tablet values, used by previews and unit tests that
         * render an overlay without a [androidx.compose.foundation.layout.BoxWithConstraints].
         */
        val Default = FrameMetrics(
            isPortrait = false,
            insetX = 48.dp,
            insetTop = 40.dp,
            insetBottom = 80.dp,
            clockSize = 168.sp,
            clockDiameter = 150.dp,
            dateSize = 32.sp,
            wordmarkSize = 20.sp,
            captionSize = 26.sp,
            ruleWidth = 280.dp,
        )
    }
}

private fun Float.clampDp(min: Float, max: Float): Dp = coerceIn(min, max).dp
private fun Float.clampSp(min: Float, max: Float): TextUnit = coerceIn(min, max).sp

/** Derive [FrameMetrics] from the frame's available size (e.g. BoxWithConstraints max*). */
fun frameMetrics(maxWidth: Dp, maxHeight: Dp): FrameMetrics {
    val w = maxWidth.value
    val h = maxHeight.value
    val vmin = minOf(w, h)
    return FrameMetrics(
        isPortrait = h >= w,
        insetX = (vmin * 0.06f).clampDp(24f, 64f),
        insetTop = (vmin * 0.05f).clampDp(22f, 52f),
        insetBottom = (vmin * 0.10f).clampDp(48f, 120f),
        clockSize = (vmin * 0.26f).clampSp(88f, 260f),
        clockDiameter = (vmin * 0.34f).clampDp(120f, 320f),
        dateSize = (vmin * 0.034f).clampSp(20f, 40f),
        wordmarkSize = (vmin * 0.046f).clampSp(14f, 28f),
        captionSize = (vmin * 0.038f).clampSp(14f, 30f),
        ruleWidth = (vmin * 0.62f).dp,
    )
}
