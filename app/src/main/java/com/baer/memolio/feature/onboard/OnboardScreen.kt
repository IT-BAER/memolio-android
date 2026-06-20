package com.baer.memolio.feature.onboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.media.QrEncoder
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.component.AmbientBackground
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.SectionHead
import com.baer.memolio.core.ui.component.WordmarkTone
import com.baer.memolio.feature.manage.addphotos.QrImage
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.baer.memolio.R

@Composable
fun OnboardScreen(
    viewModel: OnboardViewModel = hiltViewModel(),
    qrEncoder: QrEncoder = QrEncoder(),
    onFinished: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    AmbientBackground(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
            MemolioCard(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                variant = CardVariant.Raised,
                contentPadding = PaddingValues(48.dp)
            ) {
                Column {
                    MemolioWordmark(tone = WordmarkTone.Solid, size = 18.sp, modifier = Modifier.padding(bottom = 24.dp))
                    StepBody(state, qrEncoder, onOpenPaywall, viewModel)
                    Row(
                        Modifier.fillMaxWidth().padding(top = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!state.step.isFirst) {
                            MemolioButton(stringResource(R.string.action_back), viewModel::back, variant = ButtonVariant.Quiet)
                        }
                        Spacer(Modifier.weight(1f))
                        if (state.step.isLast) {
                            MemolioButton(stringResource(R.string.action_finish), { viewModel.finish(onFinished) }, variant = ButtonVariant.Primary, icon = "check")
                        } else {
                            MemolioButton(stringResource(R.string.action_next), viewModel::next, variant = ButtonVariant.Primary, icon = "arrow_forward")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBody(
    state: OnboardUiState,
    qrEncoder: QrEncoder,
    onOpenPaywall: () -> Unit,
    viewModel: OnboardViewModel
) {
    when (state.step) {
        OnboardStep.Welcome -> StepText(
            stringResource(R.string.onboard_welcome_title),
            stringResource(R.string.onboard_welcome_body)
        )
        OnboardStep.Permissions -> StepText(
            stringResource(R.string.onboard_permissions_title),
            stringResource(R.string.onboard_permissions_body)
        )
        OnboardStep.ShowQr -> {
            SectionHead(title = stringResource(R.string.onboard_qr_title), sub = stringResource(R.string.onboard_qr_sub))
            val url = state.uploadUrl
            if (url == null) {
                Body(stringResource(R.string.onboard_qr_preparing))
            } else {
                MemolioCard(variant = CardVariant.Surface, contentPadding = PaddingValues(24.dp)) {
                    Box(Modifier.size(220.dp)) { QrImage(text = url, encoder = qrEncoder) }
                }
                Body(url, modifier = Modifier.padding(top = 12.dp))
            }
        }
        OnboardStep.HomeKiosk -> {
            StepText(stringResource(R.string.onboard_homekiosk_title), stringResource(R.string.onboard_homekiosk_body))
            MemolioButton(
                stringResource(R.string.onboard_homekiosk_enable),
                { viewModel.setHomeAndKiosk(home = true, kiosk = true) },
                variant = ButtonVariant.Ghost,
                icon = "lock",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        OnboardStep.SleepSchedule -> {
            StepText(stringResource(R.string.onboard_sleep_title), stringResource(R.string.onboard_sleep_body))
            MemolioButton(
                stringResource(R.string.onboard_sleep_enable),
                { viewModel.setSleepSchedule(true, 22 * 60, 7 * 60) },
                variant = ButtonVariant.Ghost,
                icon = "schedule",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        OnboardStep.GoPro -> {
            StepText(
                stringResource(R.string.onboard_pro_title),
                stringResource(R.string.onboard_pro_body)
            )
            MemolioButton(stringResource(R.string.cta_see_pro), onOpenPaywall, variant = ButtonVariant.Secondary, icon = "auto_awesome", modifier = Modifier.padding(top = 16.dp))
        }
        OnboardStep.Finish -> StepText(stringResource(R.string.onboard_finish_title), stringResource(R.string.onboard_finish_body))
    }
}

@Composable
private fun StepText(title: String, body: String) {
    Column {
        Text(title, color = MemolioColors.TextPrimary, style = MemolioType.h2)
        Body(body, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun Body(text: String, modifier: Modifier = Modifier) {
    Text(text, color = MemolioColors.TextSecondary, style = MemolioType.body, modifier = modifier)
}
