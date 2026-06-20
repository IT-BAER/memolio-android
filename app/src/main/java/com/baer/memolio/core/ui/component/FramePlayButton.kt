package com.baer.memolio.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.Symbol

/**
 * "Play the frame" FAB: an elevated, neutral dark-glass disc holding an outlined photo
 * glyph with a small accent play-badge on its corner — the universal "playable media"
 * signal. The disc itself is neutral (no clashing solid fill); the rosy-clay accent shows
 * only in the small badge, so it reads as a deliberate floating control, not a flat color
 * blob.
 *
 * The play triangle is drawn on the Canvas because the bundled Material Symbols subset has
 * no `play_arrow` glyph.
 */
@Composable
fun FramePlayButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val shape = RoundedCornerShape(percent = 50)
    val surface = MemolioColors.Ink150
    Box(
        modifier
            .size(size)
            .shadow(10.dp, shape)
            .clip(shape)
            .background(surface, shape)
            .border(BorderStroke(1.dp, MemolioColors.BorderDefault), shape)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        // Photo glyph, sized to sit inside the disc with room for the corner badge.
        Symbol(
            name = "image",
            size = with(LocalDensity.current) { (size * 0.5f).toSp() },
            tint = MemolioColors.Paper,
        )
        // Accent play badge, inset from the bottom-end so it stays within the disc while
        // overlapping the photo glyph's corner.
        val badge = size * 0.4f
        Canvas(
            Modifier
                .size(badge)
                .align(Alignment.BottomEnd)
                .padding(end = size * 0.1f, bottom = size * 0.1f),
        ) {
            val d = this.size.minDimension
            val c = Offset(d / 2f, d / 2f)
            val ring = d * 0.12f
            val ar = d / 2f - ring
            // Thin separation ring (disc color) so the badge floats clear of the glyph.
            drawCircle(color = surface, radius = d / 2f, center = c)
            // Accent fill (clay gradient).
            drawCircle(
                brush = Brush.verticalGradient(listOf(MemolioColors.TealVivid, MemolioColors.Teal)),
                radius = ar,
                center = c,
            )
            // White play triangle, optically nudged right of center.
            val t = ar * 0.92f
            val cx = c.x + t * 0.1f
            val tri = Path().apply {
                moveTo(cx - t * 0.42f, c.y - t * 0.5f)
                lineTo(cx - t * 0.42f, c.y + t * 0.5f)
                lineTo(cx + t * 0.5f, c.y)
                close()
            }
            drawPath(tri, color = MemolioColors.Paper)
        }
    }
}
