package com.baer.memolio.feature.manage.appliance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                            SleepTimeEditor(
                                sleepStartMinutes = state.sleepStartMinutes,
                                sleepEndMinutes = state.sleepEndMinutes,
                                onTimesConfirmed = viewModel::setSleepTimes
                            )
                            MemolioSwitch(state.ambientDimming, viewModel::setAmbientDimming, label = stringResource(R.string.appliance_ambient))
                        }
                    }
                }
            )
        }
    }
}

/**
 * Two independent, tappable time rows (sleep start / wake up). Each opens its own dial
 * picker and saves IMMEDIATELY on Done — passing the other field's current value through —
 * so editing one never depends on completing the other (no lost-change trap).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimeEditor(
    sleepStartMinutes: Int,
    sleepEndMinutes: Int,
    onTimesConfirmed: (startMinutes: Int, endMinutes: Int) -> Unit
) {
    // null = no dialog open; true = editing start; false = editing wake-up
    var editing by remember { mutableStateOf<Boolean?>(null) }

    TextButton(onClick = { editing = true }) {
        Text("${stringResource(R.string.appliance_sleep_start_title)}: ${hhmm(sleepStartMinutes)}")
    }
    TextButton(onClick = { editing = false }) {
        Text("${stringResource(R.string.appliance_sleep_end_title)}: ${hhmm(sleepEndMinutes)}")
    }

    val isStart = editing
    if (isStart != null) {
        val initial = if (isStart) sleepStartMinutes else sleepEndMinutes
        val pickerState = rememberTimePickerState(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { editing = null },
            title = {
                Text(stringResource(
                    if (isStart) R.string.appliance_sleep_start_title else R.string.appliance_sleep_end_title
                ))
            },
            text = {
                // M3 sizes the big time digits from typography.displayLarge — the app maps that
                // to an 88sp hero style that overflows (clips) the picker's fixed-height field.
                // Shrink it locally to fit, and force solid high-contrast colors (the default
                // accent "wash" is 12%-alpha coral → unreadable on the dark dialog).
                val fitted = MaterialTheme.typography.copy(
                    displayLarge = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 40.sp, lineHeight = 44.sp
                    )
                )
                MaterialTheme(typography = fitted) {
                    TimePicker(
                        state = pickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MemolioColors.Ink200,
                            clockDialSelectedContentColor = MemolioColors.TextOnAccent,
                            clockDialUnselectedContentColor = MemolioColors.TextPrimary,
                            selectorColor = MemolioColors.Teal,
                            timeSelectorSelectedContainerColor = MemolioColors.Teal,
                            timeSelectorSelectedContentColor = MemolioColors.TextOnAccent,
                            timeSelectorUnselectedContainerColor = MemolioColors.Ink200,
                            timeSelectorUnselectedContentColor = MemolioColors.TextPrimary,
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val picked = pickerState.hour * 60 + pickerState.minute
                    if (isStart) onTimesConfirmed(picked, sleepEndMinutes)
                    else onTimesConfirmed(sleepStartMinutes, picked)
                    editing = null
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun hhmm(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
