package com.baer.memolio.core.billing

import android.app.Activity
import app.cash.turbine.test
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EntitlementRepositoryTest {

    private val activity: Activity = Robolectric.buildActivity(Activity::class.java).get()

    private class FakeSettings(initialPro: Boolean = false) : SettingsRepository {
        val app = MutableStateFlow(AppSettings(proUnlocked = initialPro))
        override val appSettings: Flow<AppSettings> = app
        override suspend fun setProUnlocked(value: Boolean) { app.value = app.value.copy(proUnlocked = value) }
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
        override suspend fun setOnboardingComplete(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    private class FakeClient(
        var entitled: Boolean = false,
        var purchaseResult: PurchaseResult = PurchaseResult.Success,
        var restoreResult: RestoreResult = RestoreResult.Success
    ) : RevenueCatClient {
        var refreshCalls = 0
        override suspend fun isEntitled(): Boolean { refreshCalls++; return entitled }
        override suspend fun purchase(activity: Activity): PurchaseResult = purchaseResult
        override suspend fun restore(): RestoreResult = restoreResult
    }

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun isProReflectsProUnlockedFlag() = runTest {
        val repo = EntitlementRepositoryImpl(FakeSettings(initialPro = true), FakeClient(), dispatcher)
        repo.isPro.test { assertThat(awaitItem()).isTrue() }
    }

    @Test
    fun isProIsStickyTrueAndNeverReverts() = runTest {
        val settings = FakeSettings(initialPro = false)
        val repo = EntitlementRepositoryImpl(settings, FakeClient(), dispatcher)
        repo.isPro.test {
            assertThat(awaitItem()).isFalse()
            settings.app.value = settings.app.value.copy(proUnlocked = true)
            assertThat(awaitItem()).isTrue()
            settings.app.value = settings.app.value.copy(proUnlocked = false)
            expectNoEvents()
        }
    }

    @Test
    fun refreshSetsFlagWhenEntitled() = runTest {
        val settings = FakeSettings()
        val client = FakeClient(entitled = true)
        val repo = EntitlementRepositoryImpl(settings, client, dispatcher)
        repo.refresh()
        assertThat(client.refreshCalls).isEqualTo(1)
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun refreshDoesNotClearFlagWhenNotEntitled() = runTest {
        val settings = FakeSettings(initialPro = true)
        val repo = EntitlementRepositoryImpl(settings, FakeClient(entitled = false), dispatcher)
        repo.refresh()
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun purchaseSuccessSetsFlag() = runTest {
        val settings = FakeSettings()
        val repo = EntitlementRepositoryImpl(settings, FakeClient(purchaseResult = PurchaseResult.Success), dispatcher)
        val result = repo.purchase(activity)
        assertThat(result).isEqualTo(PurchaseResult.Success)
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun purchaseCancelledDoesNotSetFlag() = runTest {
        val settings = FakeSettings()
        val repo = EntitlementRepositoryImpl(settings, FakeClient(purchaseResult = PurchaseResult.Cancelled), dispatcher)
        val result = repo.purchase(activity)
        assertThat(result).isEqualTo(PurchaseResult.Cancelled)
        assertThat(settings.app.value.proUnlocked).isFalse()
    }

    @Test
    fun purchaseErrorDoesNotSetFlag() = runTest {
        val settings = FakeSettings()
        val repo = EntitlementRepositoryImpl(settings, FakeClient(purchaseResult = PurchaseResult.Error(BillingError.PURCHASE_FAILED)), dispatcher)
        val result = repo.purchase(activity)
        assertThat(result).isInstanceOf(PurchaseResult.Error::class.java)
        assertThat(settings.app.value.proUnlocked).isFalse()
    }

    @Test
    fun restoreSuccessSetsFlag() = runTest {
        val settings = FakeSettings()
        val repo = EntitlementRepositoryImpl(settings, FakeClient(restoreResult = RestoreResult.Success), dispatcher)
        val result = repo.restore()
        assertThat(result).isEqualTo(RestoreResult.Success)
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun restoreErrorDoesNotSetFlag() = runTest {
        val settings = FakeSettings()
        val repo = EntitlementRepositoryImpl(settings, FakeClient(restoreResult = RestoreResult.Error(BillingError.RESTORE_FAILED)), dispatcher)
        val result = repo.restore()
        assertThat(result).isInstanceOf(RestoreResult.Error::class.java)
        assertThat(settings.app.value.proUnlocked).isFalse()
    }
}
