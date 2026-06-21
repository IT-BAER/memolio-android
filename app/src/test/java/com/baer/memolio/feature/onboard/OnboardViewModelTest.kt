package com.baer.memolio.feature.onboard

import app.cash.turbine.test
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.server.UploadUrlProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class OnboardViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeUploadUrlProvider : UploadUrlProvider {
        override val uploadUrl: Flow<String?> = MutableStateFlow("http://192.168.0.2:8080/?t=tok")
    }

    private class FakeSettings : SettingsRepository {
        val app = MutableStateFlow(AppSettings())
        var completed: Boolean? = null
        var kiosk: Boolean? = null
        var home: Boolean? = null
        var sleep: Triple<Boolean, Int, Int>? = null
        override val appSettings: Flow<AppSettings> = app
        override suspend fun setOnboardingComplete(value: Boolean) { completed = value; app.value = app.value.copy(onboardingComplete = value) }
        override suspend fun setKioskEnabled(value: Boolean) { kiosk = value }
        override suspend fun setHomeAppEnabled(value: Boolean) { home = value }
        override suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int) { sleep = Triple(enabled, startMinutes, endMinutes) }
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
        override suspend fun setAutostartEnabled(value: Boolean) {}
        override suspend fun setAmbientDimming(value: Boolean) {}
        override suspend fun setBrightness(value: Float) {}
        override suspend fun setAutoCleanup(value: Boolean) {}
        override suspend fun setWallpaperId(value: String) {}
        override suspend fun setProUnlocked(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    private fun vm(settings: FakeSettings = FakeSettings()) =
        OnboardViewModel(settings, FakeUploadUrlProvider()) to settings

    @Test
    fun startsOnWelcome() = runTest {
        val (model, _) = vm()
        model.state.test {
            assertThat(awaitItem().step).isEqualTo(OnboardStep.Welcome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nextAdvancesThroughEveryStepThenStopsAtFinish() = runTest {
        val (model, _) = vm()
        repeat(OnboardStep.entries.size) { model.next() }
        model.state.test {
            assertThat(awaitItem().step).isEqualTo(OnboardStep.Finish)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backFromMiddleGoesToPreviousStep() = runTest {
        val (model, _) = vm()
        model.next() // Permissions
        model.next() // ShowQr
        model.back() // Permissions
        model.state.test {
            assertThat(awaitItem().step).isEqualTo(OnboardStep.Permissions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun backOnWelcomeStays() = runTest {
        val (model, _) = vm()
        model.back()
        model.state.test {
            assertThat(awaitItem().step).isEqualTo(OnboardStep.Welcome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun finishSetsOnboardingCompleteAndInvokesCallback() = runTest {
        val (model, settings) = vm()
        var finished = false
        model.finish { finished = true }
        assertThat(settings.completed).isTrue()
        assertThat(finished).isTrue()
    }

    @Test
    fun optionalChoicesPersistFlags() = runTest {
        val (model, settings) = vm()
        model.setHomeAndKiosk(home = true, kiosk = true)
        model.setSleepSchedule(enabled = true, startMinutes = 22 * 60, endMinutes = 7 * 60)
        assertThat(settings.home).isTrue()
        assertThat(settings.kiosk).isTrue()
        assertThat(settings.sleep).isEqualTo(Triple(true, 22 * 60, 7 * 60))
    }

    @Test
    fun uploadUrlExposedForQrStep() = runTest {
        val (model, _) = vm()
        model.state.test {
            var s = awaitItem()
            if (s.uploadUrl == null) s = awaitItem()
            assertThat(s.uploadUrl).isEqualTo("http://192.168.0.2:8080/?t=tok")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun goProStepExistsBetweenSleepAndFinish() {
        val steps = OnboardStep.entries.map { it.name }
        assertThat(steps).containsExactly(
            "Welcome", "Permissions", "ShowQr", "HomeKiosk", "SleepSchedule", "GoPro", "Finish"
        ).inOrder()
    }

    @Test
    fun goProIsSkippableViaNext() = runTest {
        val (model, _) = vm()
        repeat(5) { model.next() }   // Welcome -> ... -> GoPro
        model.state.test {
            assertThat(awaitItem().step).isEqualTo(OnboardStep.GoPro)
            cancelAndIgnoreRemainingEvents()
        }
        model.next()
        model.state.test {
            assertThat(awaitItem().step).isEqualTo(OnboardStep.Finish)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
