package com.baer.memolio.feature.frame

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
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
import com.baer.memolio.core.datastore.TransitionStyle
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
import com.baer.memolio.core.ui.component.BlurredFillPhoto
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.FrameMetrics
import com.baer.memolio.core.ui.MemolioColors

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
        FrameScreen(
            state = state,
            onOpenManage = onOpenManage,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onTogglePause = viewModel::togglePause,
            onToggleFavorite = viewModel::toggleFavoriteCurrent
        )
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
 *
 * Swipe left advances to the next photo; swipe right goes to the previous photo. Single
 * tap toggles pause on the slideshow. Long-press opens Manage.
 */
@Composable
fun FrameScreen(
    state: FrameUiState,
    onOpenManage: () -> Unit,
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onTogglePause: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    var controlsRevealed by remember { mutableStateOf(false) }
    var revealNonce by remember { mutableIntStateOf(0) }
    LaunchedEffect(revealNonce) {
        if (revealNonce == 0) return@LaunchedEffect
        controlsRevealed = true
        delay(CONTROLS_REVEAL_MS)
        controlsRevealed = false
    }

    val isSlideshow = state is FrameUiState.Slideshow
    // Swipe state: accumulate horizontal drag, decide prev/next on release. `draggable`
    // coexists cleanly with `combinedClickable` (the latter still yields taps to the child
    // glass buttons — a raw `detectTapGestures` on the root swallows those child taps).
    val density = androidx.compose.ui.platform.LocalDensity.current
    val swipeThresholdPx = with(density) { 48.dp.toPx() }
    var dragTotal by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta -> dragTotal += delta }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            // Single tap reveals controls (and toggles pause in a slideshow); long-press
            // opens Manage (kiosk-safe fallback). Child glass buttons consume their own taps.
            .combinedClickable(
                onClick = {
                    revealNonce++
                    if (isSlideshow) onTogglePause()
                },
                onLongClick = onOpenManage,
                onLongClickLabel = stringResource(R.string.frame_open_settings)
            )
            .then(
                if (isSlideshow) Modifier.draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { dragTotal = 0f },
                    onDragStopped = {
                        if (dragTotal <= -swipeThresholdPx) { onNext(); revealNonce++ }
                        else if (dragTotal >= swipeThresholdPx) { onPrevious(); revealNonce++ }
                    }
                ) else Modifier
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
                // Background: transition between photos per the selected style. KEN_BURNS_CROSSFADE
                // and CROSSFADE fade between frames; SLIDE animates the new photo in horizontally;
                // CUT swaps instantly. The slow Ken Burns pan/zoom only runs in KEN_BURNS_CROSSFADE —
                // the calmer styles hold each photo still (kenBurns = false).
                val fitMode = state.fitMode
                when (state.transition) {
                    TransitionStyle.KEN_BURNS_CROSSFADE ->
                        Crossfade(state.currentPhoto, animationSpec = tween(1200), label = "photo-crossfade") { p ->
                            SlideshowPhoto(p, fitMode, kenBurns = true)
                        }
                    TransitionStyle.CROSSFADE ->
                        Crossfade(state.currentPhoto, animationSpec = tween(1200), label = "photo-crossfade") { p ->
                            SlideshowPhoto(p, fitMode, kenBurns = false)
                        }
                    TransitionStyle.SLIDE -> {
                        // Slide direction follows the nav direction: forward (next/auto) enters
                        // from the right, backward (swipe-right = previous) enters from the left.
                        val dir = if (state.advanceForward) 1 else -1
                        AnimatedContent(
                            targetState = state.currentPhoto,
                            transitionSpec = {
                                (slideInHorizontally(tween(600)) { dir * it } + fadeIn(tween(600))) togetherWith
                                    (slideOutHorizontally(tween(600)) { -dir * it } + fadeOut(tween(600)))
                            },
                            label = "photo-slide"
                        ) { p ->
                            SlideshowPhoto(p, fitMode, kenBurns = false)
                        }
                    }
                    TransitionStyle.CUT ->
                        SlideshowPhoto(state.currentPhoto, fitMode, kenBurns = false)
                }
                OverlayScrim(isPortrait = metrics.isPortrait)
                // Overlay layer — wordmark stays hidden during a slideshow; the menu button
                // and favorite button only appear after a single tap (see controlsRevealed).
                if (controlsRevealed) {
                    MenuButton(onClick = onOpenManage, metrics = metrics)
                    FavoriteButton(
                        favorite = state.currentPhoto.favorite,
                        onClick = onToggleFavorite,
                        metrics = metrics
                    )
                    // Pause indicator — shown while paused so the user knows the frame is frozen.
                    if (state.paused) {
                        PauseIndicator(metrics = metrics)
                    }
                }
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

/**
 * Heart toggle, one disc-width left of the menu button. The filled heart is drawn with a
 * Canvas because the bundled Material Symbols subset font has no filled-favorite glyph
 * (only the outline) — drawing it keeps both states crisp and tofu-free. Fills + accent-
 * tints when favorited, outline (variant tint) otherwise, with a brief scale pop on toggle.
 */
@Composable
private fun FavoriteButton(
    favorite: Boolean,
    onClick: () -> Unit,
    metrics: FrameMetrics,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (favorite) 1.15f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "favorite-scale"
    )
    val cd = stringResource(if (favorite) R.string.frame_favorite_remove else R.string.frame_favorite_add)
    Box(modifier.fillMaxSize()) {
        MemolioIconButton(
            contentDescription = cd,
            onClick = onClick,
            variant = IconButtonVariant.Glass,
            size = IconButtonSize.Md,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = metrics.insetX + IconButtonSize.Md.dim + 8.dp,
                    top = metrics.insetTop
                )
                .graphicsLayer { scaleX = scale; scaleY = scale }
        ) { tint ->
            HeartIcon(
                filled = favorite,
                color = if (favorite) MemolioColors.TealVivid else tint,
                modifier = Modifier.size(IconButtonSize.Md.dim * 0.46f)
            )
        }
    }
}

/** A heart path, filled when [filled] else stroked. Resolution-independent Canvas glyph. */
@Composable
private fun HeartIcon(
    filled: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val s = size.minDimension
        val path = Path().apply {
            moveTo(s * 0.50f, s * 0.84f)
            cubicTo(s * 0.04f, s * 0.54f, s * 0.06f, s * 0.16f, s * 0.30f, s * 0.16f)
            cubicTo(s * 0.42f, s * 0.16f, s * 0.50f, s * 0.27f, s * 0.50f, s * 0.34f)
            cubicTo(s * 0.50f, s * 0.27f, s * 0.58f, s * 0.16f, s * 0.70f, s * 0.16f)
            cubicTo(s * 0.94f, s * 0.16f, s * 0.96f, s * 0.54f, s * 0.50f, s * 0.84f)
            close()
        }
        if (filled) drawPath(path, color = color)
        else drawPath(path, color = color, style = Stroke(width = s * 0.09f))
    }
}

/**
 * Centered, semi-transparent pause glyph (two rounded bars) drawn with a Canvas — there is
 * no "pause" glyph in the bundled symbol-font subset. Not clickable, so a tap landing on it
 * still falls through to the root pause-toggle (resume). Lives inside the controlsRevealed
 * block, so it fades out with the rest of the controls.
 */
@Composable
private fun PauseIndicator(
    metrics: FrameMetrics,
    modifier: Modifier = Modifier
) {
    val cd = stringResource(R.string.frame_paused)
    val barColor = MemolioColors.Paper
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .size(IconButtonSize.Lg.dim)
                .graphicsLayer { alpha = 0.55f }
                .semantics { contentDescription = cd }
        ) {
            val s = size.minDimension
            val barW = s * 0.17f
            val barH = s * 0.46f
            val gap = s * 0.16f
            val top = (s - barH) / 2f
            val cx = s / 2f
            val r = CornerRadius(barW / 2f, barW / 2f)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(cx - gap / 2f - barW, top),
                size = Size(barW, barH),
                cornerRadius = r
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(cx + gap / 2f, top),
                size = Size(barW, barH),
                cornerRadius = r
            )
        }
    }
}

/** One slideshow frame: the shared [BlurredFillPhoto] call used by every transition style. */
@Composable
private fun SlideshowPhoto(
    photo: Photo,
    fitMode: com.baer.memolio.core.datastore.FitMode,
    kenBurns: Boolean
) {
    BlurredFillPhoto(
        model = photo.displayCachePath,
        contentDescription = photo.caption,
        kenBurns = kenBurns,
        fitMode = fitMode,
        focalX = photo.focalX,
        focalY = photo.focalY,
        modifier = Modifier.fillMaxSize()
    )
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
    metrics: FrameMetrics,
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
