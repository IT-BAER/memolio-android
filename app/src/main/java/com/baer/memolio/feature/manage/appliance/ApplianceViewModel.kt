package com.baer.memolio.feature.manage.appliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApplianceUiState(
    val autostart: Boolean = false,
    val kiosk: Boolean = false,
    val sleepEnabled: Boolean = false,
    val sleepStartMinutes: Int = 22 * 60,
    val sleepEndMinutes: Int = 7 * 60,
    val ambientDimming: Boolean = false,
    val isPro: Boolean = false
)

@HiltViewModel
class ApplianceViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val entitlement: EntitlementRepository
) : ViewModel() {

    val state: StateFlow<ApplianceUiState> =
        combine(settings.appSettings, entitlement.isPro) { s, isPro ->
            ApplianceUiState(
                autostart = s.autostartEnabled,
                kiosk = s.kioskEnabled,
                sleepEnabled = s.sleepEnabled,
                sleepStartMinutes = s.sleepStartMinutes,
                sleepEndMinutes = s.sleepEndMinutes,
                ambientDimming = s.ambientDimming,
                isPro = isPro
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApplianceUiState())

    fun setAutostart(value: Boolean) = guard { settings.setAutostartEnabled(value) }
    fun setKiosk(value: Boolean) = guard { settings.setKioskEnabled(value) }
    fun setAmbientDimming(value: Boolean) = guard { settings.setAmbientDimming(value) }
    fun setSleep(value: Boolean) = guard {
        val s = settings.appSettings.first()
        settings.setSleep(value, s.sleepStartMinutes, s.sleepEndMinutes)
    }

    fun setSleepTimes(startMinutes: Int, endMinutes: Int) = guard {
        val s = settings.appSettings.first()
        settings.setSleep(s.sleepEnabled, startMinutes, endMinutes)
    }

    /** Appliance behaviors are Pro-gated: no-op until purchased (spec section 13). */
    private fun guard(block: suspend () -> Unit) = viewModelScope.launch {
        if (entitlement.isPro.first()) block()
    }
}
