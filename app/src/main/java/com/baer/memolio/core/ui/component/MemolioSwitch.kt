package com.baer.memolio.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioMotion
import com.baer.memolio.core.ui.MemolioType

/**
 * Toggle switch — off is a glass track with a hairline; on is the teal track.
 * 50×28 track, 22dp knob, calm slide (no bounce). Matches the design Switch.
 * An optional [label] sits to the right; the whole row is the toggle target.
 */
@Composable
fun MemolioSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
) {
    val pad = 3.dp
    val knob = 22.dp
    val track = MemolioColors.Accent
    val trackColor by animateColorAsState(
        if (checked) track else MemolioColors.SurfaceGlass,
        tween(MemolioMotion.DurBase), label = "switch-track",
    )
    val knobColor by animateColorAsState(
        if (checked) MemolioColors.TextOnAccent else MemolioColors.TextSecondary,
        tween(MemolioMotion.DurBase), label = "switch-knob",
    )
    val knobOffset by animateDpAsState(
        if (checked) 50.dp - knob - pad - 1.dp else pad,
        tween(MemolioMotion.DurBase, easing = MemolioMotion.EaseOut), label = "switch-x",
    )

    Row(
        modifier
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 50.dp, height = 28.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(trackColor)
                .border(
                    1.dp,
                    if (checked) Color.Transparent else MemolioColors.BorderDefault,
                    RoundedCornerShape(percent = 50),
                ),
        ) {
            Box(
                Modifier
                    .padding(start = knobOffset, top = pad)
                    .size(knob)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(knobColor),
            )
        }
        if (label != null) {
            Text(
                text = label,
                color = MemolioColors.TextPrimary,
                style = MemolioType.body,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
