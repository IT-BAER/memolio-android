package com.baer.memolio.feature.frame

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.CaptionOverlay
import com.baer.memolio.core.ui.ClockOverlay
import com.baer.memolio.core.ui.DateOverlay
import com.baer.memolio.core.ui.MemolioBackground
import com.baer.memolio.core.ui.MemolioTheme
import com.baer.memolio.core.ui.MemolioWallpaper
import com.baer.memolio.core.ui.MenuButton
import com.baer.memolio.core.ui.OverlayScrim
import com.baer.memolio.core.ui.Wordmark

/**
 * Stateful route: collects [FrameViewModel.uiState] and passes it to the stateless
 * [FrameScreen]. [onOpenManage] is wired by :app to navigate to the Manage destination.
 */
@Composable
fun FrameRoute(
    onOpenManage: () -> Unit,
    viewModel: FrameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MemolioTheme {
        FrameScreen(state = state, onOpenManage = onOpenManage)
    }
}

/**
 * The whole frame: one Box stack — background layer -> scrim -> overlay layer — exactly
 * as the spec mandates. Stateless; everything comes from [state] and [onOpenManage].
 *
 * Long-press anywhere triggers [onOpenManage] as the kiosk-safe fallback (the menu button
 * is the primary affordance; long-press catches edge cases where it is obscured).
 */
@Composable
fun FrameScreen(
    state: FrameUiState,
    onOpenManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxSize()
            // Long-press anywhere navigates to Manage (kiosk-safe fallback).
            .combinedClickable(
                onClick = {},
                onLongClick = onOpenManage,
                onLongClickLabel = "Open settings"
            )
    ) {
        when (state) {
            is FrameUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MemolioBackground)
                        .semantics { testTag = "frame_loading" }
                )
            }

            is FrameUiState.Idle -> {
                MemolioWallpaper(
                    modifier = Modifier.fillMaxSize(),
                    driftPhase = state.driftPhase
                )
                OverlayScrim()
                // Overlay layer.
                Wordmark()
                MenuButton(onClick = onOpenManage)
                if (state.showClock) {
                    // Burn-in: nudge position by day fraction (±12dp over 24 h).
                    val driftDp = ((state.driftPhase - 0.5f) * 24f).dp
                    ClockOverlay(
                        time = state.time,
                        modifier = Modifier.graphicsLayer { translationY = driftDp.toPx() }
                    )
                }
                if (state.showDate) DateOverlay(date = state.date)
            }

            is FrameUiState.Slideshow -> {
                // Background: crossfade between photos, each with blurred-fill + Ken Burns.
                Crossfade(
                    targetState = state.currentPhoto,
                    animationSpec = tween(durationMillis = 1200),
                    label = "photo-crossfade"
                ) { photo ->
                    BlurredFillPhoto(photo = photo)
                }
                OverlayScrim()
                // Overlay layer.
                Wordmark()
                MenuButton(onClick = onOpenManage)
                if (state.showClock) ClockOverlay(time = state.time)
                if (state.showDate) DateOverlay(date = state.date)
                state.captionText?.let { CaptionOverlay(text = it) }
            }
        }
    }
}

/**
 * Blurred-fill: a heavily blurred, zoomed Crop copy fills the whole box (eliminating
 * letterbox bars for mixed aspect ratios), with the sharp Fit copy on top. Both layers
 * share a slow Ken Burns scale+translate so the still frame always feels alive.
 *
 * Scale: 1.00 -> 1.08 over 20 s, reversing smoothly.
 * Translate: ±20 px horizontal pan tracking the same progress.
 */
@Composable
private fun BlurredFillPhoto(photo: Photo, modifier: Modifier = Modifier) {
    val kbProgress by rememberInfiniteTransition(label = "ken-burns")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ken-burns-progress"
        )

    val scale = 1.0f + 0.08f * kbProgress       // 1.00 -> 1.08
    val translateX = (kbProgress - 0.5f) * 40f  // ±20 px horizontal pan

    Box(modifier.fillMaxSize()) {
        // Blurred fill: heavy blur + extra scale so the blurred content bleeds to edges.
        AsyncImage(
            model = photo.displayCachePath,
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
            model = photo.displayCachePath,
            contentDescription = photo.caption,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.translationX = translateX
                }
        )
    }
}
