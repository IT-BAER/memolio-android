package com.baer.memolio.feature.manage.addphotos

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baer.memolio.core.media.QrEncoder

/**
 * Renders [text] as a QR code by delegating to the Phase-2 [QrEncoder] (ZXing).
 * The bitmap is recomputed only when [text] or [size] changes.
 */
@Composable
fun QrImage(
    text: String,
    encoder: QrEncoder,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val bitmap = remember(text, sizePx) { encoder.encode(text, sizePx).asImageBitmap() }
    Image(bitmap = bitmap, contentDescription = "Upload QR code", modifier = modifier)
}
