package com.baer.memolio.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol

/**
 * Outlined text input — glass fill, hairline border that turns teal on focus
 * (with a soft focus ring), 10dp radius. Optional [label] above and leading
 * [icon] glyph. Matches the design Input.
 *
 * Structured like M3's TextField: [BasicTextField] is the semantics root and the
 * label lives inside its decoration, so the field is discoverable (and editable)
 * by its label text for accessibility tooling and tests.
 */
@Composable
fun MemolioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    icon: String? = null,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(10.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        interactionSource = interaction,
        textStyle = MemolioType.body.copy(color = MemolioColors.TextPrimary),
        cursorBrush = SolidColor(MemolioColors.Accent),
        decorationBox = { inner ->
            Column {
                if (label != null) {
                    Text(
                        text = label,
                        color = MemolioColors.TextSecondary,
                        style = MemolioType.sm,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Row(
                    Modifier
                        .background(MemolioColors.SurfaceGlass, shape)
                        .border(
                            if (focused) 2.dp else 1.dp,
                            if (focused) MemolioColors.Accent else MemolioColors.BorderDefault,
                            shape,
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        Symbol(icon, size = 20.sp, tint = MemolioColors.TextTertiary, modifier = Modifier.padding(end = 10.dp))
                    }
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, color = MemolioColors.TextTertiary, style = MemolioType.body)
                        }
                        inner()
                    }
                }
            }
        },
    )
}
