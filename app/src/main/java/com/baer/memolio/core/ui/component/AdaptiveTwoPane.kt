package com.baer.memolio.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lays two equal panes side-by-side (Row, each `weight(1f)`) when there's room, and
 * stacks them vertically (Column, each `fillMaxWidth`) when the available width drops
 * below [stackBelow]. Used by the Manage cards so the landscape two-column layout reflows
 * cleanly to a single column in portrait instead of cramping/clipping.
 *
 * Each pane receives the [Modifier] it must apply to its root so the correct
 * weight/fill is wired by the branch.
 */
@Composable
fun AdaptiveTwoPane(
    modifier: Modifier = Modifier,
    spacing: Dp = 20.dp,
    stackBelow: Dp = 640.dp,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        if (maxWidth < stackBelow) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                first(Modifier.weight(1f))
                second(Modifier.weight(1f))
            }
        }
    }
}
