package com.baer.memolio.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.baer.memolio.core.ui.MemolioColors

/** Card surface variants, mirroring the design's `variant` prop. */
enum class CardVariant { Surface, Raised, Glass }

private data class CardStyle(val bg: Color, val border: Color, val elevation: androidx.compose.ui.unit.Dp)

private fun cardStyle(variant: CardVariant): CardStyle = when (variant) {
    CardVariant.Surface -> CardStyle(MemolioColors.SurfaceCard, MemolioColors.BorderSoft, 12.dp)
    CardVariant.Raised -> CardStyle(MemolioColors.SurfaceCardRaised, MemolioColors.BorderDefault, 20.dp)
    // Glass: translucent frosted panel. Compose can't blur content behind, so the
    // frost is approximated with a strong glass fill over whatever sits beneath.
    CardVariant.Glass -> CardStyle(MemolioColors.SurfaceGlassStrong, MemolioColors.BorderDefault, 12.dp)
}

/**
 * Surface card — the warm-dark panel used throughout Manage. Low, diffuse shadow;
 * hairline border; 14dp radius. [interactive] adds a clickable affordance.
 */
@Composable
fun MemolioCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Surface,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    interactive: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val s = cardStyle(variant)
    val shape = RoundedCornerShape(14.dp)
    var m = modifier
        .shadow(s.elevation, shape, spotColor = Color.Black, ambientColor = Color.Black)
        .clip(shape)
        .background(s.bg, shape)
        .border(BorderStroke(1.dp, s.border), shape)
    if (interactive && onClick != null) m = m.clickable(onClick = onClick)
    Box(m.padding(contentPadding), content = content)
}
