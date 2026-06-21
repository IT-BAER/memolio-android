package com.baer.memolio.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.baer.memolio.core.datastore.FitMode

/**
 * Maps a normalized focal point (each 0..1) to a Crop alignment. BiasAlignment bias runs
 * -1..1 (-1 = left/top, 1 = right/bottom), so bias = focal*2 - 1. A null focal (no face
 * detected) falls back to Alignment.Center — the prior always-center-crop behavior.
 */
internal fun focalAlignment(focalX: Float?, focalY: Float?): Alignment =
    if (focalX != null && focalY != null) {
        BiasAlignment(
            horizontalBias = focalX.coerceIn(0f, 1f) * 2f - 1f,
            verticalBias = focalY.coerceIn(0f, 1f) * 2f - 1f,
        )
    } else {
        Alignment.Center
    }

/**
 * Renders one photo to fill the frame per [fitMode]:
 *  - [FitMode.BLURRED_FILL] ("bokeh"): a heavily blurred, zoomed Crop copy fills the box
 *    (no letterbox bars for mixed aspect ratios) under a sharp Fit copy.
 *  - [FitMode.CROP]: a single Crop copy fills the box, keeping aspect ratio and cropping
 *    the overflow. The crop biases toward the detected face ([focalX]/[focalY]) when
 *    available, falling back to center when the focal point is null. No blur.
 *  - [FitMode.FIT_BARS]: the whole photo (Fit) on plain black bars. No blur.
 *
 * Shared by the slideshow frame and the library preview. The library passes the default
 * [FitMode.BLURRED_FILL] so curation always shows the whole photo.
 *
 * When [kenBurns] is true every mode shares a slow scale Ken Burns (1.00 -> 1.08 over 20 s,
 * reversing) so a still frame feels alive; the library preview passes false (the user is
 * examining the photo, motion would fight a swipe).
 */
@Composable
fun BlurredFillPhoto(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    kenBurns: Boolean = true,
    fitMode: FitMode = FitMode.BLURRED_FILL,
    focalX: Float? = null,
    focalY: Float? = null,
) {
    val kbProgress by rememberInfiniteTransition(label = "ken-burns")
        .animateFloat(
            initialValue = 0f,
            targetValue = if (kenBurns) 1f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ken-burns-progress"
        )

    val scale = 1.0f + 0.08f * kbProgress       // 1.00 -> 1.08 (or a static 1.00)

    Box(modifier.fillMaxSize()) {
        when (fitMode) {
            FitMode.BLURRED_FILL -> {
                // Blurred fill: heavy blur + extra scale so the blurred content bleeds to the edges.
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(48.dp)
                        .graphicsLayer {
                            scaleX = scale * 1.15f
                            scaleY = scale * 1.15f
                        }
                )
                // Sharp contained photo on top, sharing the Ken Burns transform.
                AsyncImage(
                    model = model,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            FitMode.CROP -> {
                // Fill the frame, keep aspect ratio, crop the overflow. No blur backdrop.
                // Crop biases toward the detected face focal point; falls back to center.
                AsyncImage(
                    model = model,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    alignment = focalAlignment(focalX, focalY),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            FitMode.FIT_BARS -> {
                // Whole photo on plain black bars.
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    AsyncImage(
                        model = model,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
            }
        }
    }
}
