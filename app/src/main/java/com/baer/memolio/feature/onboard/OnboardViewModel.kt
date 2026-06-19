package com.baer.memolio.feature.onboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.server.UploadUrlProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardUiState(
    val step: OnboardStep = OnboardStep.Welcome,
    val uploadUrl: String? = null
)

@HiltViewModel
class OnboardViewModel @Inject constructor(
    private val settings: SettingsRepository,
    uploadUrlProvider: UploadUrlProvider
) : ViewModel() {

    private val step = MutableStateFlow(OnboardStep.Welcome)

    val state: StateFlow<OnboardUiState> =
        combine(step, uploadUrlProvider.uploadUrl) { s, url ->
            OnboardUiState(step = s, uploadUrl = url)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardUiState())

    fun next() = step.update { it.nextOrSelf() }
    fun back() = step.update { it.previousOrSelf() }

    fun setHomeAndKiosk(home: Boolean, kiosk: Boolean) = viewModelScope.launch {
        settings.setHomeAppEnabled(home)
        settings.setKioskEnabled(kiosk)
    }

    fun setSleepSchedule(enabled: Boolean, startMinutes: Int, endMinutes: Int) = viewModelScope.launch {
        settings.setSleep(enabled, startMinutes, endMinutes)
    }

    /** Completes onboarding: persist the gate flag, then let the host navigate to Frame. */
    fun finish(onFinished: () -> Unit) = viewModelScope.launch {
        settings.setOnboardingComplete(true)
        onFinished()
    }
}
