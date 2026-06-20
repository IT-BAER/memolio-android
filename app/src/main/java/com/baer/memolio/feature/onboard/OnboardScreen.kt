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
                            MemolioButton("Back", viewModel::back, variant = ButtonVariant.Quiet)
                        }
                        Spacer(Modifier.weight(1f))
                        if (state.step.isLast) {
                            MemolioButton("Finish", { viewModel.finish(onFinished) }, variant = ButtonVariant.Primary, icon = "check")
                        } else {
                            MemolioButton("Next", viewModel::next, variant = ButtonVariant.Primary, icon = "arrow_forward")
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
            "Welcome to Memolio",
            "Let's turn this tablet into a quiet photo frame for the people and places you " +
                "want close. It takes about a minute."
        )
        OnboardStep.Permissions -> StepText(
            "Permissions",
            "Memolio needs notification and boot permissions so the frame keeps running on " +
                "its own. It only ever touches its own folder, never your gallery."
        )
        OnboardStep.ShowQr -> {
            SectionHead(title = "Add your first photos", sub = "Scan from any phone on the same Wi-Fi")
            val url = state.uploadUrl
            if (url == null) {
                Body("Preparing the upload link… (make sure Wi-Fi is on)")
            } else {
                MemolioCard(variant = CardVariant.Surface, contentPadding = PaddingValues(24.dp)) {
                    Box(Modifier.size(220.dp)) { QrImage(text = url, encoder = qrEncoder) }
                }
                Body(url, modifier = Modifier.padding(top = 12.dp))
            }
        }
        OnboardStep.HomeKiosk -> {
            StepText("Home & kiosk", "Optional: set Memolio as the Home app and lock it (kiosk).")
            MemolioButton(
                "Enable Home + kiosk",
                { viewModel.setHomeAndKiosk(home = true, kiosk = true) },
                variant = ButtonVariant.Ghost,
                icon = "lock",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        OnboardStep.SleepSchedule -> {
            StepText("Sleep schedule", "Optional: sleep the screen overnight (22:00–07:00).")
            MemolioButton(
                "Enable sleep schedule",
                { viewModel.setSleepSchedule(true, 22 * 60, 7 * 60) },
                variant = ButtonVariant.Ghost,
                icon = "schedule",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        OnboardStep.GoPro -> {
            StepText(
                "Memolio Pro",
                "Optional: unlock albums, the appliance suite, and custom wallpapers with a " +
                    "one-time purchase. You can do this any time later."
            )
            MemolioButton("See Pro", onOpenPaywall, variant = ButtonVariant.Secondary, icon = "auto_awesome", modifier = Modifier.padding(top = 16.dp))
        }
        OnboardStep.Finish -> StepText("All set", "Tap Finish to start the frame.")
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
