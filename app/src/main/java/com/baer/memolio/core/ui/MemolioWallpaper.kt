package com.baer.memolio.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Resolution-independent default wallpaper, ported from docs/mockups/wallpaper-only.html.
 * Every CSS `linear-gradient(<deg>, ...)` becomes a [Brush.linearGradient] whose start
 * and end are derived from the canvas size via [gradientVector], so the look is identical
 * at any density. No raster assets ship — this is pure vector math, themeable and tiny.
 *
 * [driftPhase] in 0f..1f slowly migrates the whole composition (burn-in mitigation);
 * pass a value derived from the time of day. 0f produces zero offset.
 */
@Composable
fun MemolioWallpaper(
    modifier: Modifier = Modifier,
    driftPhase: Float = 0f,
    isPortrait: Boolean = false
) {
    Box(
        modifier
            .fillMaxSize()
            .drawBehind { drawMemolioWallpaper(driftPhase, isPortrait) }
    )
}

/**
 * Start/end points for a CSS-style gradient angle on [size].
 * CSS 0deg points up and increases clockwise; we map it to canvas space (y down).
 * The line passes through the canvas center; length scales with the canvas so the
 * direction is resolution-independent (verified in MemolioWallpaperTest).
 */
fun gradientVector(angleDeg: Float, size: Size): Pair<Offset, Offset> {
    val rad = (angleDeg * PI / 180.0)
    // CSS up = -y; clockwise positive.
    val dx = sin(rad).toFloat()
    val dy = -cos(rad).toFloat()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val half = maxOf(size.width, size.height) / 2f
    val start = Offset(cx - dx * half, cy - dy * half)
    val end = Offset(cx + dx * half, cy + dy * half)
    return start to end
}

/** Continuous circular drift (loops cleanly at phase 0==1) bounded by [radiusPx]. */
fun idleDriftOffset(phase: Float, radiusPx: Float): Offset {
    val a = (phase * 2.0 * PI)
    return Offset((cos(a) * radiusPx).toFloat(), (sin(a) * radiusPx).toFloat())
}

private fun DrawScope.linear(
    angleDeg: Float,
    stops: Array<Pair<Float, Color>>
): Brush {
    val (start, end) = gradientVector(angleDeg, size)
    return Brush.linearGradient(colorStops = stops, start = start, end = end)
}

/**
 * The full layered stack from the mockup, painted back-to-front:
 *  1. base 118deg deep gradient (#07080c -> #10151c -> #221b27 -> #100e0d)
 *  2. 26deg teal sliver, 205deg amber sliver, 148deg top-left sheen
 *  3. skewed ambient panel (bottom-right): teal+white wash, ~58% opacity
 *  4. `::after` edge-darkening scrim (left+bottom heavier)
 * [driftPhase] translates the whole thing within a small radius for burn-in.
 */
fun DrawScope.drawMemolioWallpaper(driftPhase: Float = 0f, isPortrait: Boolean = false) {
    val drift = idleDriftOffset(driftPhase, radiusPx = size.minDimension * 0.012f)
    // Overscan the whole composition ~5% about center. The burn-in drift below translates
    // the layers by up to ~1.2% of the short side. idleDriftOffset is a circle of constant
    // radius (it is NOT zero at phase 0, since cos(0)=1), so without this margin that constant
    // shift would expose the backing color as a hairline band at the trailing edge (most
    // visibly the left edge in Manage, which holds driftPhase at 0).
    scale(scaleX = 1.05f, scaleY = 1.05f, pivot = center) {
    translate(drift.x, drift.y) {
        // 1. base deep diagonal
        drawRect(
            linear(
                118f,
                arrayOf(
                    0.00f to Color(0xFF07080C),
                    0.31f to Color(0xFF10151C),
                    0.67f to Color(0xFF221B27),
                    1.00f to Color(0xFF100E0D)
                )
            )
        )
        // 2a. top-left white sheen (148deg)
        drawRect(
            linear(
                148f,
                arrayOf(
                    0.00f to Color.White.copy(alpha = 0.045f),
                    0.24f to Color.Transparent
                )
            )
        )
        // 2b. teal sliver (26deg, band 42%..61%)
        drawRect(
            linear(
                26f,
                arrayOf(
                    0.42f to Color.Transparent,
                    0.47f to MemolioAccentTeal.copy(alpha = 0.12f),
                    0.61f to Color.Transparent
                )
            )
        )
        // 2c. amber sliver (205deg, band 38%..58%)
        drawRect(
            linear(
                205f,
                arrayOf(
                    0.38f to Color.Transparent,
                    0.46f to MemolioAccentAmber.copy(alpha = 0.12f),
                    0.58f to Color.Transparent
                )
            )
        )
        // 3. skewed ambient panel, bottom-right. Approximate the CSS skewX(-12deg)
        //    panel as a parallelogram filled with a teal+white wash.
        val panelW = size.width * 0.58f
        val panelH = size.height * 0.64f
        val left = size.width - panelW * 0.86f
        val top = size.height - panelH * 0.84f
        val skew = panelH * 0.21f // tan(12deg) ~= 0.213 of the height
        val panel = Path().apply {
            moveTo(left + skew, top)
            lineTo(left + panelW + skew, top)
            lineTo(left + panelW, top + panelH)
            lineTo(left, top + panelH)
            close()
        }
        drawPath(
            path = panel,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to Color.White.copy(alpha = 0.06f),
                    0.32f to Color.Transparent,
                    0.58f to MemolioAccentTeal.copy(alpha = 0.10f),
                    1.00f to Color.Transparent
                ),
                start = Offset(left, top),
                end = Offset(left + panelW, top + panelH)
            ),
            alpha = 0.58f
        )
    }
    // 4. `::after` edge scrim — drawn OUTSIDE the drift so the darkened frame edges
    //    always cover the canvas corners regardless of drift translation. Follows
    //    orientation: portrait uses the bottom-heavy `--wall-scrim-portrait`; landscape
    //    keeps the left+bottom `--wall-scrim-x` + `--wall-scrim-y`.
    if (isPortrait) {
        drawRect(
            linear(
                180f,
                arrayOf(
                    0.00f to Color.Black.copy(alpha = 0.30f),
                    0.22f to Color.Transparent,
                    0.50f to Color.Transparent,
                    1.00f to Color.Black.copy(alpha = 0.66f)
                )
            )
        )
    } else {
        drawRect(
            linear(
                90f,
                arrayOf(
                    0.00f to Color.Black.copy(alpha = 0.54f),
                    0.34f to Color.Transparent,
                    1.00f to Color.Black.copy(alpha = 0.28f)
                )
            )
        )
        drawRect(
            linear(
                180f,
                arrayOf(
                    0.00f to Color.Black.copy(alpha = 0.36f),
                    0.34f to Color.Transparent,
                    1.00f to Color.Black.copy(alpha = 0.48f)
                )
            )
        )
    }
    }
}
