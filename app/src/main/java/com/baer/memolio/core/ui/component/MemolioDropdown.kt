package com.baer.memolio.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol

/**
 * One selectable row in a [MemolioDropdown]. [systemFont] forces [FontFamily.Default]
 * (instead of the brand Inter face) for glyph-unsafe scripts — e.g. CJK autonyms that
 * Inter's bundled subset cannot render.
 */
data class DropdownItem(val key: String, val label: String, val systemFont: Boolean = false)

/**
 * Compact single-select dropdown — a textfield-styled anchor (glass fill, hairline border
 * that turns accent when open, leading [leadingIcon], a rotating chevron caret) that opens a
 * Material 3 menu of [items]. The selected row carries a check. Replaces long inline option
 * lists where vertical space matters (e.g. the language picker).
 */
@Composable
fun MemolioDropdown(
    items: List<DropdownItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = items.firstOrNull { it.key == selectedKey } ?: items.firstOrNull()
    val shape = RoundedCornerShape(10.dp)

    Box(modifier) {
        Row(
            Modifier
                .clip(shape)
                .background(MemolioColors.SurfaceGlass, shape)
                .border(
                    if (expanded) 2.dp else 1.dp,
                    if (expanded) MemolioColors.Accent else MemolioColors.BorderDefault,
                    shape,
                )
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Symbol(leadingIcon, size = 20.sp, tint = MemolioColors.TextTertiary, modifier = Modifier.padding(end = 12.dp))
            }
            Text(
                selected?.label.orEmpty(),
                color = MemolioColors.TextPrimary,
                style = if (selected?.systemFont == true) MemolioType.body.copy(fontFamily = FontFamily.Default) else MemolioType.body,
                modifier = Modifier.weight(1f),
            )
            // chevron_right rotated to point down (closed) / up (open) — the subset has no
            // dedicated expand glyph, so we reuse the right-chevron.
            Symbol(
                "chevron_right",
                size = 22.sp,
                tint = MemolioColors.TextSecondary,
                modifier = Modifier.padding(start = 8.dp).rotate(if (expanded) -90f else 90f),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 220.dp),
        ) {
            items.forEach { item ->
                val isSelected = item.key == selectedKey
                DropdownMenuItem(
                    text = {
                        Text(
                            item.label,
                            color = if (isSelected) MemolioColors.TextPrimary else MemolioColors.TextSecondary,
                            style = if (item.systemFont) MemolioType.body.copy(fontFamily = FontFamily.Default) else MemolioType.body,
                        )
                    },
                    trailingIcon = if (isSelected) {
                        { Symbol("check", size = 20.sp, tint = MemolioColors.Teal) }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelect(item.key)
                    },
                )
            }
        }
    }
}
