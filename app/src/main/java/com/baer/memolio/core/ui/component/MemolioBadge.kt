package com.baer.memolio.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol

/** Badge tones, mirroring the design's `tone` prop. */
enum class BadgeTone { Neutral, Pro, Teal, Success, Warn, Error }

private data class BadgeColors(val bg: Color, val fg: Color, val border: Color)

private fun badgeColors(tone: BadgeTone): BadgeColors = when (tone) {
    BadgeTone.Neutral -> BadgeColors(MemolioColors.SurfaceGlass, MemolioColors.TextSecondary, MemolioColors.BorderDefault)
    BadgeTone.Pro -> BadgeColors(MemolioColors.AmberWash, MemolioColors.AmberSoft, MemolioColors.Amber.copy(alpha = 0.4f))
    BadgeTone.Teal -> BadgeColors(MemolioColors.TealWash, MemolioColors.TealSoft, MemolioColors.Teal.copy(alpha = 0.4f))
    BadgeTone.Success -> BadgeColors(MemolioColors.Ok.copy(alpha = 0.12f), MemolioColors.Ok, MemolioColors.Ok.copy(alpha = 0.4f))
    BadgeTone.Warn -> BadgeColors(MemolioColors.Warn.copy(alpha = 0.12f), MemolioColors.Warn, MemolioColors.Warn.copy(alpha = 0.4f))
    BadgeTone.Error -> BadgeColors(MemolioColors.Error.copy(alpha = 0.12f), MemolioColors.Error, MemolioColors.Error.copy(alpha = 0.4f))
}

/**
 * Small status / category chip — the "Pro" lock badge, upload states, metadata
 * tags. Pill-shaped, uppercase, tinted by [tone]. Optional leading [icon] glyph.
 */
@Composable
fun MemolioBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: BadgeTone = BadgeTone.Neutral,
    icon: String? = null,
) {
    val c = badgeColors(tone)
    Row(
        modifier
            .background(c.bg, RoundedCornerShape(percent = 50))
            .border(1.dp, c.border, RoundedCornerShape(percent = 50))
            .padding(PaddingValues(horizontal = 12.dp, vertical = 4.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Symbol(icon, size = 14.sp, tint = c.fg, modifier = Modifier.padding(end = 6.dp))
        }
        Text(
            text = text.uppercase(),
            color = c.fg,
            style = MemolioType.label,
            fontSize = 12.sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}
