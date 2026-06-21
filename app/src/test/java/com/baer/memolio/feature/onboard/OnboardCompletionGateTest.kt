package com.baer.memolio.feature.onboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.server.UploadUrlProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardCompletionGateTest {

    @get:Rule val composeRule = createComposeRule()

    private class FakeUploadUrlProvider : UploadUrlProvider {
        override val uploadUrl: Flow<String?> = MutableStateFlow(null)
    }

    private class FakeSettings : SettingsRepository {
        val app = MutableStateFlow(AppSettings())
        override val appSettings: Flow<AppSettings> = app
        override suspend fun setOnboardingComplete(value: Boolean) { app.value = app.value.copy(onboardingComplete = value) }
        override val playlistConfig: Flow<PlaylistConfig> = MutableStateFlow(PlaylistConfig())
        override suspend fun setActiveAlbumIds(ids: Set<String>) {}
        override suspend fun setShuffle(value: Boolean) {}
        override suspend fun setIntervalSeconds(value: Int) {}
        override suspend fun setTransition(value: TransitionStyle) {}
        override suspend fun setFitMode(value: FitMode) {}
        override suspend fun setShowClock(value: Boolean) {}
        override suspend fun setShowDate(value: Boolean) {}
        override suspend fun setShowCaption(value: Boolean) {}
        override suspend fun setClockStyle(value: ClockStyle) = Unit
        override suspend fun setClockOpacity(value: Float) = Unit
        override suspend fun setClockScale(value: Float) = Unit
        override suspend fun setUploadToken(token: String) {}
        override suspend fun setServerPort(port: Int) {}
        override suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int) {}
        override suspend fun setKioskEnabled(value: Boolean) {}
        override suspend fun setHomeAppEnabled(value: Boolean) {}
        override suspend fun setAutostartEnabled(value: Boolean) {}
        override suspend fun setAmbientDimming(value: Boolean) {}
        override suspend fun setBrightness(value: Float) {}
        override suspend fun setAutoCleanup(value: Boolean) {}
        override suspend fun setWallpaperId(value: String) {}
        override suspend fun setProUnlocked(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    @Test
    fun steppingToFinishAndTappingFinishSetsGateAndInvokesCallback() {
        val settings = FakeSettings()
        val vm = OnboardViewModel(settings, FakeUploadUrlProvider())
        var finished = false

        composeRule.setContent {
            OnboardScreen(viewModel = vm, onFinished = { finished = true })
        }

        // Welcome -> Permissions -> ShowQr -> HomeKiosk -> SleepSchedule -> GoPro -> Finish (6 Next taps)
        repeat(6) {
            composeRule.onNodeWithText("Next").performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithText("Finish").assertIsDisplayed()
        composeRule.onNodeWithText("Finish").performClick()
        composeRule.waitForIdle()

        assertThat(settings.app.value.onboardingComplete).isTrue()
        assertThat(finished).isTrue()
    }
}
