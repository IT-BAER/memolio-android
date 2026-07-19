package com.baer.memolio.feature.onboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
import com.baer.memolio.core.media.QrEncoder
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.component.AmbientBackground
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone
import com.baer.memolio.core.ui.frameMetrics
import com.baer.memolio.feature.manage.addphotos.QrImage
import kotlinx.coroutines.delay

/** CTA state derived purely from the current step — no Compose needed, unit-testable. */
data class OnboardCta(val showBack: Boolean, val isFinish: Boolean)

internal fun onboardCta(step: OnboardStep) =
    OnboardCta(showBack = !step.isFirst, isFinish = step.isLast)

@Composable
fun OnboardScreen(
    viewModel: OnboardViewModel = hiltViewModel(),
    qrEncoder: QrEncoder = QrEncoder(),
    onFinished: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    OnboardContent(
        state = state,
        qrEncoder = qrEncoder,
        onNext = viewModel::next,
        onBack = viewModel::back,
        onFinish = { viewModel.finish(onFinished) },
        onEnableAppliance = { viewModel.setHomeAndKiosk(home = true, kiosk = true) },
        onEnableSleep = { viewModel.setSleepSchedule(true, 22 * 60, 7 * 60) },
        onOpenPaywall = onOpenPaywall,
        modifier = modifier
    )
}

@Composable
internal fun OnboardContent(
    state: OnboardUiState,
    qrEncoder: QrEncoder = QrEncoder(),
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onEnableAppliance: () -> Unit,
    onEnableSleep: () -> Unit,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    AmbientBackground(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val metrics = frameMetrics(maxWidth, maxHeight)
            MemolioWordmark(
                tone = WordmarkTone.Solid,
                size = metrics.wordmarkSize,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = metrics.insetX, top = metrics.insetTop)
            )
            // Wordmark clearance on top + fillMaxSize give the content column a bounded
            // height, so the weighted step area scrolls instead of overgrowing the
            // viewport (landscape QR step slid under the wordmark and off-screen).
            val wordmarkClearance = with(LocalDensity.current) { metrics.wordmarkSize.toDp() * 2 }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = metrics.insetX)
                    .padding(top = metrics.insetTop + wordmarkClearance, bottom = metrics.insetBottom),
                verticalArrangement = Arrangement.Bottom
            ) {
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        val dir = if (forward) 1 else -1
                        (slideInHorizontally(
                            spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)
                        ) { it / 6 * dir } + fadeIn()) togetherWith
                            (slideOutHorizontally(
                                spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)
                            ) { -it / 6 * dir } + fadeOut())
                    },
                    label = "onboardStep",
                    modifier = Modifier.weight(1f, fill = false)
                ) { step ->
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        StepBody(step, state.uploadUrl, qrEncoder, onEnableAppliance, onEnableSleep, onOpenPaywall)
                    }
                }
                ProgressDots(current = state.step, modifier = Modifier.padding(top = 24.dp))
                CtaRow(
                    step = state.step,
                    onNext = onNext,
                    onBack = onBack,
                    onFinish = onFinish,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 80L); shown = true }
    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }
    ) { content() }
}

@Composable
private fun ProgressDots(current: OnboardStep, modifier: Modifier = Modifier) {
    val total = OnboardStep.entries.size
    val cd = stringResource(R.string.onboard_step_progress_cd, current.ordinal + 1, total)
    Row(
        modifier.semantics { contentDescription = cd },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OnboardStep.entries.forEach { s ->
            val active = s == current
            val width by animateDpAsState(if (active) 28.dp else 8.dp, spring(), label = "dotW")
            val color by animateColorAsState(
                if (active) MemolioColors.Accent else MemolioColors.TextSecondary.copy(alpha = 0.35f),
                label = "dotC"
            )
            Box(
                Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun StepBody(
    step: OnboardStep,
    uploadUrl: String?,
    qrEncoder: QrEncoder,
    onEnableAppliance: () -> Unit,
    onEnableSleep: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    when (step) {
        OnboardStep.Welcome -> {
            StaggerIn(0) {
                Text(
                    stringResource(R.string.onboard_welcome_title),
                    color = MemolioColors.TextPrimary,
                    style = MemolioType.h1
                )
            }
            StaggerIn(1) {
                Text(
                    stringResource(R.string.onboard_welcome_body),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.bodyLg,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        OnboardStep.ShowQr -> {
            StaggerIn(0) {
                Text(
                    stringResource(R.string.onboard_qr_title),
                    color = MemolioColors.TextPrimary,
                    style = MemolioType.h1
                )
            }
            StaggerIn(1) {
                Text(
                    stringResource(R.string.onboard_qr_sub),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.bodyLg,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            StaggerIn(2) {
                if (uploadUrl == null) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                        label = "alpha"
                    )
                    Text(
                        stringResource(R.string.onboard_qr_preparing),
                        color = MemolioColors.TextSecondary.copy(alpha = alpha),
                        style = MemolioType.body,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                } else {
                    var qrShown by remember(uploadUrl) { mutableStateOf(false) }
                    LaunchedEffect(uploadUrl) { qrShown = true }
                    val qrScale by animateFloatAsState(
                        if (qrShown) 1f else 0.85f, spring(dampingRatio = 0.7f), label = "qr"
                    )
                    val qrAlpha by animateFloatAsState(
                        if (qrShown) 1f else 0f, tween(300), label = "qrA"
                    )
                    // Single child: AnimatedVisibility stacks multiple children like a Box,
                    // which overlapped the URL text onto the QR. Centered per design.
                    Column(
                        Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MemolioCard(
                            variant = CardVariant.Surface,
                            contentPadding = PaddingValues(20.dp),
                            modifier = Modifier.graphicsLayer { scaleX = qrScale; scaleY = qrScale; alpha = qrAlpha }
                        ) {
                            Box(Modifier.size(200.dp)) { QrImage(text = uploadUrl, encoder = qrEncoder) }
                        }
                        Text(
                            uploadUrl,
                            color = MemolioColors.TextSecondary,
                            style = MemolioType.body,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
        OnboardStep.Appliance -> {
            StaggerIn(0) {
                Text(
                    stringResource(R.string.onboard_appliance_title),
                    color = MemolioColors.TextPrimary,
                    style = MemolioType.h1
                )
            }
            StaggerIn(1) {
                Text(
                    stringResource(R.string.onboard_appliance_body),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.bodyLg,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            StaggerIn(2) {
                Column(
                    Modifier.padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MemolioButton(
                        stringResource(R.string.onboard_homekiosk_enable),
                        onEnableAppliance,
                        variant = ButtonVariant.Ghost,
                        icon = "lock"
                    )
                    MemolioButton(
                        stringResource(R.string.onboard_sleep_enable),
                        onEnableSleep,
                        variant = ButtonVariant.Ghost,
                        icon = "schedule"
                    )
                }
            }
        }
        OnboardStep.GoPro -> {
            StaggerIn(0) {
                Text(
                    stringResource(R.string.onboard_pro_title),
                    color = MemolioColors.TextPrimary,
                    style = MemolioType.h1
                )
            }
            StaggerIn(1) {
                Text(
                    stringResource(R.string.onboard_pro_body),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.bodyLg,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            StaggerIn(2) {
                MemolioButton(
                    stringResource(R.string.cta_see_pro),
                    onOpenPaywall,
                    variant = ButtonVariant.Secondary,
                    icon = "auto_awesome",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
        OnboardStep.Finish -> {
            StaggerIn(0) {
                Text(
                    stringResource(R.string.onboard_finish_title),
                    color = MemolioColors.TextPrimary,
                    style = MemolioType.h1
                )
            }
            StaggerIn(1) {
                Text(
                    stringResource(R.string.onboard_finish_body),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.bodyLg,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CtaRow(
    step: OnboardStep,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (!step.isFirst) {
            MemolioButton(stringResource(R.string.action_back), onBack, variant = ButtonVariant.Quiet)
        }
        Spacer(Modifier.weight(1f))
        if (step.isLast) {
            val pulse = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
                pulse.animateTo(1.06f, tween(250))
                pulse.animateTo(1f, spring(dampingRatio = 0.5f))
            }
            MemolioButton(
                stringResource(R.string.action_finish),
                onFinish,
                variant = ButtonVariant.Primary,
                icon = "check",
                modifier = Modifier.graphicsLayer { scaleX = pulse.value; scaleY = pulse.value }
            )
        } else {
            MemolioButton(
                stringResource(R.string.action_next),
                onNext,
                variant = ButtonVariant.Primary,
                icon = "arrow_forward"
            )
        }
    }
}
