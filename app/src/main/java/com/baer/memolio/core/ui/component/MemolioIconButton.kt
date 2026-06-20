package com.baer.memolio.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.Symbol

/** Icon-button visual variants, mirroring the design's `variant` prop. */
enum class IconButtonVariant { Glass, Solid, Bare }

/** Icon-button sizes (disc diameter): sm 40 / md 52 / lg 64. */
enum class IconButtonSize(val dim: Dp) { Sm(40.dp), Md(52.dp), Lg(64.dp) }

/**
 * Circular glass icon button — the frame's signature "menu" affordance. A
 * translucent disc with a hairline ring; [IconButtonVariant.Solid] is the teal
 * accent disc. Glyph is sized 0.42× the diameter, matching the mockup.
 */
@Composable
fun MemolioIconButton(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: IconButtonVariant = IconButtonVariant.Glass,
    size: IconButtonSize = IconButtonSize.Md,
    enabled: Boolean = true,
) {
    val pill = RoundedCornerShape(percent = 50)
    val dim = size.dim

    val bgBrush: Brush?
    val bgColor: Color
    val tint: Color
    val border: BorderStroke?
    when (variant) {
        IconButtonVariant.Glass -> {
            bgBrush = null; bgColor = MemolioColors.SurfaceGlass
            tint = MemolioColors.TextSecondary
            border = BorderStroke(1.dp, MemolioColors.BorderDefault)
        }
        IconButtonVariant.Solid -> {
            bgBrush = Brush.verticalGradient(listOf(MemolioColors.TealVivid, MemolioColors.Teal)); bgColor = Color.Transparent
            tint = MemolioColors.Ink000
            border = BorderStroke(1.dp, MemolioColors.TealVivid)
        }
        IconButtonVariant.Bare -> {
            bgBrush = null; bgColor = Color.Transparent
            tint = MemolioColors.TextSecondary
            border = null
        }
    }

    var m = modifier
        .size(dim)
        .then(if (variant == IconButtonVariant.Solid) Modifier.shadow(10.dp, pill, spotColor = MemolioColors.TealVivid, ambientColor = MemolioColors.TealVivid) else Modifier)
        .clip(pill)
        .then(if (bgBrush != null) Modifier.background(bgBrush, pill) else Modifier.background(bgColor, pill))
    if (border != null) m = m.border(border, pill)

    Box(
        m.clickable(enabled = enabled, onClick = onClick)
            .semantics { this.contentDescription = contentDescription; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Symbol(icon, size = with(androidx.compose.ui.platform.LocalDensity.current) { (dim * 0.42f).toSp() }, tint = tint)
    }
}
