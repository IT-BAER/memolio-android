package com.baer.memolio.feature.onboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.media.QrEncoder
import com.baer.memolio.feature.manage.addphotos.QrImage

@Composable
fun OnboardScreen(
    viewModel: OnboardViewModel = hiltViewModel(),
    qrEncoder: QrEncoder = QrEncoder(),
    onFinished: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        when (state.step) {
            OnboardStep.Welcome ->
                Text("Welcome to Memolio, turn this tablet into a photo frame.")
            OnboardStep.Permissions ->
                Text("We need notification + boot permissions to keep the frame running. " +
                    "Photos stay in the app's own folder, no media access requested.")
            OnboardStep.ShowQr -> {
                Text("Scan to add your first photos:")
                val url = state.uploadUrl
                if (url == null) Text("Preparing the upload link… (make sure Wi-Fi is on)")
                else { QrImage(text = url, encoder = qrEncoder); Text(url) }
            }
            OnboardStep.HomeKiosk -> {
                Text("Optional: set Memolio as the Home app and lock it (kiosk).")
                Button(onClick = { viewModel.setHomeAndKiosk(home = true, kiosk = true) }) {
                    Text("Enable Home + kiosk")
                }
            }
            OnboardStep.SleepSchedule -> {
                Text("Optional: sleep the screen overnight (10pm-7am).")
                Button(onClick = { viewModel.setSleepSchedule(true, 22 * 60, 7 * 60) }) {
                    Text("Enable sleep schedule")
                }
            }
            OnboardStep.GoPro -> {
                Text("Optional: unlock Memolio Pro (one-time purchase) for albums, the " +
                    "appliance suite, and custom wallpapers. You can do this any time later.")
                Button(onClick = onOpenPaywall) { Text("See Pro") }
            }
            OnboardStep.Finish ->
                Text("All set! Tap Finish to start the frame.")
        }

        Row(modifier = Modifier.padding(top = 24.dp)) {
            if (!state.step.isFirst) {
                Button(onClick = viewModel::back) { Text("Back") }
            }
            if (state.step.isLast) {
                Button(onClick = { viewModel.finish(onFinished) }) { Text("Finish") }
            } else {
                Button(onClick = viewModel::next) { Text("Next") }
            }
        }
    }
}
