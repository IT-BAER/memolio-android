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
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone

/**
 * Shared overlay composables used in both the idle home and slideshow states.
 * Styling is the Memolio design system (docs/design/Memolio Frame.html FrameView):
 * sizes target the landscape-tablet upper bound of the mockup's clamp() rules.
 */

/** Large, light clock — bottom-left. Design: Thin weight, 0.82 line-height, soft drop. */
@Composable
fun ClockOverlay(
    time: String,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Text(
            text = time,
            color = MemolioColors.Paper,
            style = MemolioType.clock,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 90.dp, bottom = 110.dp, end = 24.dp)
        )
    }
}

/** Date line + quiet gradient rule — sits just above the clock block at bottom-left. */
@Composable
fun DateOverlay(
    date: String,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 90.dp, bottom = 70.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = date,
                color = MemolioColors.Paper680,
                style = MemolioType.h1,
            )
            // The mockup's quiet rule: thin gradient fading left → transparent.
            Box(
                Modifier
                    .width(280.dp)
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
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return
    Box(modifier.fillMaxSize()) {
        Text(
            text = text,
            color = MemolioColors.Paper680,
            fontSize = 28.sp,
            fontFamily = InterFamily,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 72.dp, bottom = 72.dp, start = 96.dp)
        )
    }
}

/** Faint uppercase brand mark — top-left. Design: faint tone (paper 34%), 20sp. */
@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        MemolioWordmark(
            tone = WordmarkTone.Faint,
            size = 20.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 56.dp, top = 48.dp)
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
                .padding(end = 56.dp, top = 44.dp)
        )
    }
}

/**
 * Legibility scrim between the background layer and the overlays — the design's
 * `--wall-scrim-x` + `--wall-scrim-y`: heavier on the left and bottom edges (where
 * the clock/date live) so light photos never wash out the text.
 */
@Composable
fun OverlayScrim(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
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
    )
}
