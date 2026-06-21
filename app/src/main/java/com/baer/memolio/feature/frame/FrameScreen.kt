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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.baer.memolio.R
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.ui.AnalogClockOverlay
import com.baer.memolio.core.ui.CaptionOverlay
import com.baer.memolio.core.ui.ClockOverlay
import com.baer.memolio.core.ui.DateOverlay
import com.baer.memolio.core.ui.frameMetrics
import com.baer.memolio.core.ui.MemolioBackground
import com.baer.memolio.core.ui.MemolioTheme
import com.baer.memolio.core.ui.WallpaperBackground
import com.baer.memolio.core.ui.MenuButton
import com.baer.memolio.core.ui.OverlayScrim
import com.baer.memolio.core.ui.Wordmark

/** How long the slideshow menu button stays visible after a tap before fading out. */
private const val CONTROLS_REVEAL_MS = 4_000L

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
 *
 * During a photo slideshow the frame stays uncluttered: the wordmark is hidden and the menu
 * button only appears on a single tap, fading again after [CONTROLS_REVEAL_MS]. On the
 * default wallpaper (Idle) the wordmark + menu button are always shown.
 */
@Composable
fun FrameScreen(
    state: FrameUiState,
    onOpenManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var controlsRevealed by remember { mutableStateOf(false) }
    var revealNonce by remember { mutableIntStateOf(0) }
    LaunchedEffect(revealNonce) {
        if (revealNonce == 0) return@LaunchedEffect
        controlsRevealed = true
        delay(CONTROLS_REVEAL_MS)
        controlsRevealed = false
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            // Single tap reveals the slideshow controls; long-press navigates to Manage
            // (kiosk-safe fallback).
            .combinedClickable(
                onClick = { revealNonce++ },
                onLongClick = onOpenManage,
                onLongClickLabel = stringResource(R.string.frame_open_settings)
            )
    ) {
        // Overlay sizes/insets re-flow off the short edge — portrait-first, both orientations.
        val metrics = frameMetrics(maxWidth, maxHeight)
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
                WallpaperBackground(
                    wallpaperId = state.wallpaperId,
                    modifier = Modifier.fillMaxSize(),
                    driftPhase = state.driftPhase,
                    isPortrait = metrics.isPortrait,
                    customImagePath = state.customWallpaperPath
                )
                OverlayScrim(isPortrait = metrics.isPortrait)
                // Overlay layer.
                Wordmark(metrics = metrics)
                MenuButton(onClick = onOpenManage, metrics = metrics)
                if (state.showClock) {
                    // Burn-in: nudge position by day fraction (±12dp over 24 h).
                    val driftDp = ((state.driftPhase - 0.5f) * 24f).dp
                    StyledClock(
                        style = state.clockStyle,
                        time = state.time,
                        hour = state.hour,
                        minute = state.minute,
                        showDate = state.showDate,
                        opacity = state.clockOpacity,
                        sizeScale = state.clockScale,
                        metrics = metrics,
                        modifier = Modifier.graphicsLayer { translationY = driftDp.toPx() }
                    )
                }
                if (state.showDate) DateOverlay(date = state.date, metrics = metrics)
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
                OverlayScrim(isPortrait = metrics.isPortrait)
                // Overlay layer — wordmark stays hidden during a slideshow; the menu button
                // only appears after a single tap (see controlsRevealed).
                if (controlsRevealed) MenuButton(onClick = onOpenManage, metrics = metrics)
                if (state.showClock) StyledClock(
                    style = state.clockStyle,
                    time = state.time,
                    hour = state.hour,
                    minute = state.minute,
                    showDate = state.showDate,
                    opacity = state.clockOpacity,
                    sizeScale = state.clockScale,
                    metrics = metrics
                )
                if (state.showDate) DateOverlay(date = state.date, metrics = metrics)
                state.captionText?.let { CaptionOverlay(text = it, metrics = metrics) }
            }
        }
    }
}

/** Renders the digital or analog clock per [style]; shares position/inset/date-lift. */
@Composable
private fun StyledClock(
    style: ClockStyle,
    time: String,
    hour: Int,
    minute: Int,
    showDate: Boolean,
    opacity: Float,
    sizeScale: Float,
    metrics: com.baer.memolio.core.ui.FrameMetrics,
    modifier: Modifier = Modifier
) {
    when (style) {
        ClockStyle.DIGITAL ->
            ClockOverlay(
                time = time, metrics = metrics, liftAboveDate = showDate,
                opacity = opacity, sizeScale = sizeScale, modifier = modifier
            )
        ClockStyle.ANALOG ->
            AnalogClockOverlay(
                hour = hour, minute = minute, contentDescription = time,
                metrics = metrics, liftAboveDate = showDate,
                opacity = opacity, sizeScale = sizeScale, modifier = modifier
            )
    }
}

/**
 * Blurred-fill: a heavily blurred, zoomed Crop copy fills the whole box (eliminating
 * letterbox bars for mixed aspect ratios), with the sharp Fit copy on top. Both layers
 * share a slow Ken Burns scale+translate so the still frame always feels alive.
 *
 * Ken Burns is scale-only (design: scale 1.00 -> 1.08 over 20 s, reversing smoothly) so
 * it reads identically in portrait and landscape; the whole idle composition's slow
 * burn-in drift lives in the wallpaper/clock, not here.
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
                }
        )
    }
}
