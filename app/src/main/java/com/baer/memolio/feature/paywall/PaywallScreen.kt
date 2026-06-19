package com.baer.memolio.feature.paywall

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text("Memolio Pro")
        Text(
            "A one-time purchase. No subscription, no ads. Unlocks albums & playlists, " +
                "the appliance suite, and custom wallpapers, yours forever, offline."
        )

        when {
            state.isPro -> {
                Text("Pro is unlocked. Thank you!")
                Button(onClick = onClose) { Text("Done") }
            }
            state.offline -> {
                Text("Connect to Wi-Fi to unlock Pro. The rest of Memolio works offline.")
                TextButton(onClick = onClose) { Text("Maybe later") }
            }
            else -> {
                if (state.loading) CircularProgressIndicator()
                state.offerings.forEach { title -> Text(title) }
                state.error?.let { Text("Error: $it") }
                Button(
                    enabled = !state.loading && activity != null,
                    onClick = { activity?.let { viewModel.purchase(it) } }
                ) { Text("Unlock Pro") }
                TextButton(
                    enabled = !state.loading,
                    onClick = { viewModel.restore() }
                ) { Text("Restore purchase") }
                TextButton(onClick = onClose) { Text("Maybe later") }
            }
        }
    }
}
