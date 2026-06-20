@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.baer.memolio.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType

/**
 * Horizontal slider — the playlist interval control. Teal fill on a thin glass
 * track (4dp), a 18dp paper knob ringed in teal. Optional [label]/[suffix] header
 * with a tabular value readout. Matches the design Slider.
 */
@Composable
fun MemolioSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    label: String? = null,
    suffix: String = "",
    enabled: Boolean = true,
) {
    Column(modifier) {
        if (label != null || suffix.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label.orEmpty(), color = MemolioColors.TextSecondary, style = MemolioType.sm)
                Text("$value$suffix", color = MemolioColors.Accent, style = MemolioType.sm)
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange,
            enabled = enabled,
            track = { state ->
                val span = (state.valueRange.endInclusive - state.valueRange.start).coerceAtLeast(1f)
                val frac = ((state.value - state.valueRange.start) / span).coerceIn(0f, 1f)
                Canvas(Modifier.fillMaxWidth().height(24.dp)) {
                    val h = 4.dp.toPx()
                    val cy = size.height / 2f
                    val r = CornerRadius(h / 2f, h / 2f)
                    drawRoundRect(
                        color = MemolioColors.SurfaceGlass,
                        topLeft = Offset(0f, cy - h / 2f),
                        size = Size(size.width, h),
                        cornerRadius = r,
                    )
                    drawRoundRect(
                        color = MemolioColors.BorderSoft,
                        topLeft = Offset(0f, cy - h / 2f),
                        size = Size(size.width, h),
                        cornerRadius = r,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                    )
                    drawRoundRect(
                        color = MemolioColors.Accent,
                        topLeft = Offset(0f, cy - h / 2f),
                        size = Size(size.width * frac, h),
                        cornerRadius = r,
                    )
                }
            },
            thumb = {
                Box(
                    Modifier
                        .size(18.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MemolioColors.Paper)
                        .border(3.dp, MemolioColors.Accent, CircleShape),
                )
            },
        )
    }
}
