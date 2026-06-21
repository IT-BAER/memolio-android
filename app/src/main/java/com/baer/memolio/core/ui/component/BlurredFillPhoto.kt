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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.baer.memolio.core.datastore.FitMode

/**
 * Renders one photo to fill the frame per [fitMode]:
 *  - [FitMode.BLURRED_FILL] ("bokeh"): a heavily blurred, zoomed Crop copy fills the box
 *    (no letterbox bars for mixed aspect ratios) under a sharp Fit copy.
 *  - [FitMode.CROP]: a single Crop copy fills the box, keeping aspect ratio and cropping
 *    the overflow. No blur (also avoids Modifier.blur, a no-op below API 31).
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
                AsyncImage(
                    model = model,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
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
