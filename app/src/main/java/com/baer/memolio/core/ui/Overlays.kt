package com.baer.memolio.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

/**
 * Shared overlay composables used in both the idle home and slideshow states.
 * Styling is ported from docs/mockups/home-wallpaper.html.
 * Sizes are dp/sp approximations of the mockup's clamp() viewport rules targeting
 * the upper clamp bound (landscape tablet = the real frame device).
 */

/** Large, light clock — bottom-left. Mockup: weight 270, big text-shadow, tabular nums. */
@Composable
fun ClockOverlay(
    time: String,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Text(
            text = time,
            color = MemolioInk,
            fontSize = 168.sp,
            lineHeight = 138.sp,  // 0.82 * fontSize, matching mockup line-height: 0.82
            fontWeight = FontWeight.Thin,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 96.dp, bottom = 132.dp, end = 24.dp)
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
                .padding(start = 96.dp, bottom = 84.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = date,
                color = MemolioMuted,
                fontSize = 40.sp,
                fontWeight = FontWeight.Light
            )
            // Mockup's `.quiet-line`: thin gradient rule fading left → transparent.
            Box(
                Modifier
                    .width(320.dp)
                    .height(1.dp)
                    .drawBehind {
                        drawRect(
                            Brush.horizontalGradient(
                                0f to MemolioInk.copy(alpha = 0.42f),
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
            color = MemolioMuted,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 72.dp, bottom = 72.dp, start = 96.dp)
        )
    }
}

/** Faint uppercase brand mark — top-left. Mockup: 0.34 alpha, weight 500, 0.18em tracking. */
@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Text(
            text = "MEMOLIO",
            color = MemolioWordmarkColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 56.dp, top = 48.dp)
        )
    }
}

/**
 * Circular glass hamburger button — top-right.
 * The icon is three rounded lines drawn via Canvas, matching the mockup's
 * `.menu-lines` element rather than pulling in material-icons-core.
 * Content description "Open settings" satisfies TalkBack + the UI test assertion.
 */
@Composable
fun MenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 56.dp, top = 44.dp)
                .size(60.dp)
                .background(MemolioGlass, CircleShape)
                .border(1.dp, MemolioGlassStroke, CircleShape)
                .clickable(
                    onClick = onClick,
                    onClickLabel = "Open settings",
                    role = Role.Button
                )
                .semantics {
                    contentDescription = "Open settings"
                    role = Role.Button
                }
        ) {
            // Three hamburger lines drawn in canvas — 42% of button width per mockup.
            Box(
                Modifier
                    .size(width = 25.dp, height = 17.dp)
                    .drawWithContent {
                        val lineH = 2.dp.toPx()
                        val r = lineH / 2f
                        val lineW = size.width
                        val gap = (size.height - lineH * 3) / 2f
                        val lineColor = MemolioMuted
                        for (i in 0..2) {
                            val top = i * (lineH + gap)
                            drawRoundRect(
                                color = lineColor,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                                size = androidx.compose.ui.geometry.Size(lineW, lineH),
                                cornerRadius = CornerRadius(r, r)
                            )
                        }
                    }
            )
        }
    }
}

/**
 * Legibility scrim between the background layer and the overlays.
 * Mirrors the mockup's `.wallpaper::after` — heavier on the left and bottom edges
 * (where the clock/date live) so light photos never wash out the text.
 */
@Composable
fun OverlayScrim(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                // Horizontal: strong black left (clock side), lighter right.
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.54f),
                        0.34f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.28f)
                    )
                )
                // Vertical: top edge + stronger bottom (clock/date gravity).
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
