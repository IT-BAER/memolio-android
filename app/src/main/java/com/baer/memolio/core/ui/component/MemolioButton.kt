package com.baer.memolio.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol

/** Button visual variants, mirroring the design's `variant` prop. */
enum class ButtonVariant { Primary, Secondary, Ghost, Quiet }

/** Button sizes, mirroring the design's `size` prop. */
enum class ButtonSize { Sm, Md, Lg }

private data class BtnSizing(val padH: Dp, val padV: Dp, val minHeight: Dp, val gap: Dp, val font: TextUnit)

private fun sizing(size: ButtonSize): BtnSizing = when (size) {
    ButtonSize.Sm -> BtnSizing(16.dp, 8.dp, 36.dp, 8.dp, 14.sp)
    ButtonSize.Md -> BtnSizing(22.dp, 12.dp, 44.dp, 10.dp, 16.sp)
    ButtonSize.Lg -> BtnSizing(28.dp, 15.dp, 52.dp, 12.dp, 18.sp)
}

/**
 * Memolio primary action button — calm, hairline-bordered, warm. Accent fills use
 * the sage-teal ([ButtonVariant.Primary]) or clay-amber ([ButtonVariant.Secondary])
 * brand gradients with a soft colored glow; ghost is text-on-glass; quiet is bare.
 */
@Composable
fun MemolioButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Md,
    icon: String? = null,
    enabled: Boolean = true,
    maxLines: Int = 1,
) {
    val s = sizing(size)
    val pill = RoundedCornerShape(percent = 50)
    val filled = variant == ButtonVariant.Primary || variant == ButtonVariant.Secondary

    // Resolve per-variant fill/text/border.
    val fillBrush: Brush?
    val solidFill: Color
    val textColor: Color
    val border: BorderStroke?
    val glow: Color
    when (variant) {
        ButtonVariant.Primary -> {
            fillBrush = Brush.verticalGradient(listOf(MemolioColors.TealVivid, MemolioColors.Teal))
            solidFill = Color.Transparent
            textColor = MemolioColors.Ink000
            border = BorderStroke(1.dp, MemolioColors.TealVivid)
            glow = MemolioColors.TealVivid.copy(alpha = 0.45f)
        }
        ButtonVariant.Secondary -> {
            fillBrush = Brush.verticalGradient(listOf(MemolioColors.AmberVivid, MemolioColors.Amber))
            solidFill = Color.Transparent
            textColor = MemolioColors.Ink000
            border = BorderStroke(1.dp, MemolioColors.AmberVivid)
            glow = MemolioColors.AmberVivid.copy(alpha = 0.45f)
        }
        ButtonVariant.Ghost -> {
            fillBrush = null
            solidFill = MemolioColors.SurfaceGlass
            textColor = MemolioColors.TextPrimary
            border = BorderStroke(1.dp, MemolioColors.BorderDefault)
            glow = Color.Transparent
        }
        ButtonVariant.Quiet -> {
            fillBrush = null
            solidFill = Color.Transparent
            textColor = MemolioColors.TextSecondary
            border = null
            glow = Color.Transparent
        }
    }

    // Disabled reads unmistakably flat & drained.
    val disabledFill = MemolioColors.Ink200
    var base = modifier
        .defaultMinSize(minHeight = s.minHeight)
        .then(
            if (enabled && filled) Modifier.shadow(10.dp, pill, ambientColor = glow, spotColor = glow)
            else Modifier
        )
        .clip(pill)

    base = when {
        !enabled && filled -> base.background(disabledFill, pill)
        fillBrush != null -> base.background(fillBrush, pill)
        else -> base.background(solidFill, pill)
    }
    val effectiveBorder = if (!enabled) BorderStroke(1.dp, MemolioColors.BorderSoft) else border
    if (effectiveBorder != null) base = base.border(effectiveBorder, pill)

    Row(
        base
            .clickable(enabled = enabled, onClick = onClick)
            .padding(PaddingValues(horizontal = s.padH, vertical = s.padV)),
        horizontalArrangement = Arrangement.spacedBy(s.gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = if (enabled) textColor else MemolioColors.TextDisabled
        if (icon != null) Symbol(icon, size = s.font * 1.25f, tint = content)
        Text(
            text = text,
            color = content,
            fontFamily = MemolioType.body.fontFamily,
            fontSize = s.font,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.01.em,
            maxLines = maxLines,
            softWrap = maxLines > 1,
            textAlign = TextAlign.Center,
        )
    }
}
