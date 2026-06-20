package com.baer.memolio.feature.manage.appliance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.ui.ProGate
import com.baer.memolio.core.ui.component.AdaptiveTwoPane
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
        SectionHead(title = stringResource(R.string.appliance_title), sub = stringResource(R.string.appliance_subtitle))
        ProGate(feature = ProFeature.APPLIANCE, isPro = isPro, onUpsell = onOpenPaywall) {
            AdaptiveTwoPane(
                first = { paneModifier ->
                    MemolioCard(modifier = paneModifier, variant = CardVariant.Surface) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            MemolioSwitch(state.autostart, viewModel::setAutostart, label = stringResource(R.string.appliance_autostart))
                            MemolioSwitch(state.kiosk, viewModel::setKiosk, label = stringResource(R.string.appliance_kiosk))
                        }
                    }
                },
                second = { paneModifier ->
                    MemolioCard(modifier = paneModifier, variant = CardVariant.Surface) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            MemolioSwitch(
                                state.sleepEnabled,
                                viewModel::setSleep,
                                label = stringResource(R.string.appliance_sleep, hhmm(state.sleepStartMinutes), hhmm(state.sleepEndMinutes))
                            )
                            MemolioSwitch(state.ambientDimming, viewModel::setAmbientDimming, label = stringResource(R.string.appliance_ambient))
                        }
                    }
                }
            )
        }
    }
}

private fun hhmm(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
