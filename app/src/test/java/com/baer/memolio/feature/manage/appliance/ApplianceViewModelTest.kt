package com.baer.memolio.feature.manage.appliance

import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
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

class ApplianceViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: android.app.Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

    private class FakeSettings(initial: AppSettings = AppSettings()) : SettingsRepository {
        val app = MutableStateFlow(initial)
        data class SleepCall(val enabled: Boolean, val startMinutes: Int, val endMinutes: Int)
        var lastSleepCall: SleepCall? = null

        override val appSettings: Flow<AppSettings> = app
        override suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
            lastSleepCall = SleepCall(enabled, startMinutes, endMinutes)
            app.value = app.value.copy(sleepEnabled = enabled, sleepStartMinutes = startMinutes, sleepEndMinutes = endMinutes)
        }
        override val playlistConfig: Flow<PlaylistConfig> = MutableStateFlow(PlaylistConfig())
        override suspend fun setWallpaperId(value: String) {}
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
        override suspend fun setKioskEnabled(value: Boolean) {}
        override suspend fun setHomeAppEnabled(value: Boolean) {}
        override suspend fun setAutostartEnabled(value: Boolean) {}
        override suspend fun setAmbientDimming(value: Boolean) {}
        override suspend fun setBrightness(value: Float) {}
        override suspend fun setAutoCleanup(value: Boolean) {}
        override suspend fun setOnboardingComplete(value: Boolean) {}
        override suspend fun setProUnlocked(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    @Test
    fun `setSleepTimes persists new times when Pro`() = runTest {
        val settings = FakeSettings(AppSettings(sleepEnabled = true, sleepStartMinutes = 22 * 60, sleepEndMinutes = 7 * 60))
        val vm = ApplianceViewModel(settings, FakeEntitlement(pro = true))

        vm.setSleepTimes(8 * 60, 6 * 60)

        val call = settings.lastSleepCall
        assertThat(call).isNotNull()
        assertThat(call!!.enabled).isTrue()
        assertThat(call.startMinutes).isEqualTo(480)
        assertThat(call.endMinutes).isEqualTo(360)
    }

    @Test
    fun `setSleepTimes is a no-op when not Pro`() = runTest {
        val settings = FakeSettings(AppSettings(sleepEnabled = false, sleepStartMinutes = 22 * 60, sleepEndMinutes = 7 * 60))
        val vm = ApplianceViewModel(settings, FakeEntitlement(pro = false))

        vm.setSleepTimes(8 * 60, 6 * 60)

        assertThat(settings.lastSleepCall).isNull()
    }
}
