package com.baer.memolio.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone

/**
 * Shared overlay composables used in both the idle home and slideshow states.
 * Styling is the Memolio design system (claude.ai/design "Memolio Design System",
 * FrameView). Sizes and insets are driven off the SHORT edge via [FrameMetrics] so the
 * one composition re-flows identically in portrait (primary) and landscape (secondary).
 * Each overlay defaults to [FrameMetrics.Default] (≈ landscape tablet) so previews/tests
 * render without a BoxWithConstraints; the live frame passes real metrics.
 */

/** Large, light clock — bottom-left. Design: Thin weight, 0.82 line-height, soft drop. */
@Composable
fun ClockOverlay(
    time: String,
    metrics: FrameMetrics = FrameMetrics.Default,
    liftAboveDate: Boolean = true,
    modifier: Modifier = Modifier
) {
    // When the date block is shown below, lift the clock to clear the date + rule.
    val dateReserve = if (liftAboveDate) (metrics.dateSize.value * 1.5f + 18f).dp else 0.dp
    Box(modifier.fillMaxSize()) {
        Text(
            text = time,
            color = MemolioColors.Paper,
            style = MemolioType.clock.copy(
                fontSize = metrics.clockSize,
                lineHeight = metrics.clockSize * 0.82f,
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = metrics.insetX, bottom = metrics.insetBottom + dateReserve, end = metrics.insetX)
        )
    }
}

/** Date line + quiet gradient rule — sits at the bottom-left, beneath the clock. */
@Composable
fun DateOverlay(
    date: String,
    metrics: FrameMetrics = FrameMetrics.Default,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = metrics.insetX, bottom = metrics.insetBottom, end = metrics.insetX),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = date,
                color = MemolioColors.Paper680,
                style = MemolioType.h1.copy(
                    fontSize = metrics.dateSize,
                    lineHeight = metrics.dateSize * 1.2f,
                ),
            )
            // The design's quiet rule: thin gradient fading left → transparent.
            Box(
                Modifier
                    .width(metrics.ruleWidth)
                    .height(1.dp)
                    .drawBehind {
                        drawRect(
                            Brush.horizontalGradient(
                                0f to MemolioColors.Paper420,
                                1f to Color.Transparent
                            )
                        )
                    }
            )
        }
    }
}

/**
 * Per-photo caption — bottom-right so it never collides with the clock block.
 * Blank/whitespace text renders nothing (no placeholder space).
 */
@Composable
fun CaptionOverlay(
    text: String,
    metrics: FrameMetrics = FrameMetrics.Default,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return
    Box(modifier.fillMaxSize()) {
        Text(
            text = text,
            color = MemolioColors.Paper680,
            style = MemolioType.body.copy(fontSize = metrics.captionSize),
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Leave the clock room on the left (start inset ×2).
                .padding(end = metrics.insetX, bottom = metrics.insetBottom, start = metrics.insetX * 2)
        )
    }
}

/** Faint uppercase brand mark — top-left. Design: faint tone (paper 34%). */
@Composable
fun Wordmark(
    metrics: FrameMetrics = FrameMetrics.Default,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        MemolioWordmark(
            tone = WordmarkTone.Faint,
            size = metrics.wordmarkSize,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = metrics.insetX, top = metrics.insetTop)
        )
    }
}

/**
 * Circular glass "menu" affordance — top-right. The design-system [MemolioIconButton]
 * (glass disc, hairline ring, Material Symbols "menu" glyph). Content description
 * "Open settings" satisfies TalkBack + the UI test assertion.
 */
@Composable
fun MenuButton(
    onClick: () -> Unit,
    metrics: FrameMetrics = FrameMetrics.Default,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        MemolioIconButton(
            icon = "menu",
            contentDescription = "Open settings",
            onClick = onClick,
            variant = IconButtonVariant.Glass,
            size = IconButtonSize.Md,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = metrics.insetX, top = metrics.insetTop)
        )
    }
}

/**
 * Legibility scrim between the background layer and the overlays. Follows orientation:
 * - Portrait ([isPortrait] = true): the design's `--wall-scrim-portrait` — bottom-heavy
 *   (the clock sits low) with a soft top wash for the wordmark/menu row.
 * - Landscape: `--wall-scrim-x` + `--wall-scrim-y` — heavier on the left and bottom edges.
 */
@Composable
fun OverlayScrim(
    isPortrait: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                if (isPortrait) {
                    drawRect(
                        Brush.verticalGradient(
                            0.00f to Color.Black.copy(alpha = 0.30f),
                            0.22f to Color.Transparent,
                            0.50f to Color.Transparent,
                            1.00f to Color.Black.copy(alpha = 0.66f)
                        )
                    )
                } else {
                    drawRect(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.54f),
                            0.34f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.28f)
                        )
                    )
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.36f),
                            0.34f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.48f)
                        )
                    )
                }
            }
    )
}
