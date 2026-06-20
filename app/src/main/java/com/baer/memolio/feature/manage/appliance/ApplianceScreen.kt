package com.baer.memolio.feature.manage.appliance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.ui.ProGate
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioSwitch
import com.baer.memolio.core.ui.component.SectionHead

@Composable
fun ApplianceScreen(
    isPro: Boolean,
    onOpenPaywall: () -> Unit = {},
    viewModel: ApplianceViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier) {
        SectionHead(title = "Appliance", sub = "Run Memolio like a fixed device")
        ProGate(feature = ProFeature.APPLIANCE, isPro = isPro, onUpsell = onOpenPaywall) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                MemolioCard(modifier = Modifier.weight(1f), variant = CardVariant.Surface) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MemolioSwitch(state.autostart, viewModel::setAutostart, label = "Auto-start on boot")
                        MemolioSwitch(state.kiosk, viewModel::setKiosk, label = "Kiosk lock (Home app)")
                    }
                }
                MemolioCard(modifier = Modifier.weight(1f), variant = CardVariant.Surface) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MemolioSwitch(
                            state.sleepEnabled,
                            viewModel::setSleep,
                            label = "Sleep screen ${hhmm(state.sleepStartMinutes)}–${hhmm(state.sleepEndMinutes)}"
                        )
                        MemolioSwitch(state.ambientDimming, viewModel::setAmbientDimming, label = "Ambient brightness dimming")
                    }
                }
            }
        }
    }
}

private fun hhmm(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
