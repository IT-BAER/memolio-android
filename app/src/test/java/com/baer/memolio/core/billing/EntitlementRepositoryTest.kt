package com.baer.memolio.core.billing

import app.cash.turbine.test
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.TransitionSpeed
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.time.TimeProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
class EntitlementRepositoryTest {

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
        override suspend fun setTransitionSpeed(value: TransitionSpeed) {}
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
        override suspend fun setPurchasePendingSince(epochDay: Long) { app.value = app.value.copy(purchasePendingSince = epochDay) }
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    private class FakeClient(
        var entitled: Boolean = false,
        var failure: Exception? = null
    ) : RevenueCatClient {
        var refreshCalls = 0
        override suspend fun isEntitled(): Boolean {
            refreshCalls++
            failure?.let { throw it }
            return entitled
        }
    }

    private val dispatcher = UnconfinedTestDispatcher()

    // 30 October 2026 = epoch day 20756.
    private val time = object : TimeProvider {
        override fun now(): LocalDateTime = LocalDateTime.of(2026, 10, 30, 12, 0)
    }
    private val today = 20756L

    @Test
    fun isProReflectsProUnlockedFlag() = runTest {
        val repo = EntitlementRepositoryImpl(FakeSettings(initialPro = true), FakeClient(), dispatcher, time)
        repo.isPro.test { assertThat(awaitItem()).isTrue() }
    }

    @Test
    fun isProTurnsFalseWhenTheFlagIsCleared() = runTest {
        val settings = FakeSettings(initialPro = true)
        val repo = EntitlementRepositoryImpl(settings, FakeClient(), dispatcher, time)
        repo.isPro.test {
            assertThat(awaitItem()).isTrue()
            settings.app.value = settings.app.value.copy(proUnlocked = false)
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun refreshSetsFlagWhenEntitled() = runTest {
        val settings = FakeSettings()
        val client = FakeClient(entitled = true)
        val repo = EntitlementRepositoryImpl(settings, client, dispatcher, time)
        repo.refresh()
        assertThat(client.refreshCalls).isEqualTo(1)
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun refreshRelocksWhenThePurchaseIsNoLongerActive() = runTest {
        val settings = FakeSettings(initialPro = true)
        val repo = EntitlementRepositoryImpl(settings, FakeClient(entitled = false), dispatcher, time)
        repo.refresh()
        assertThat(settings.app.value.proUnlocked).isFalse()
    }

    @Test
    fun refreshKeepsTheFlagWhenTheCheckFails() = runTest {
        val settings = FakeSettings(initialPro = true)
        val client = FakeClient(failure = IllegalStateException("offline"))
        val repo = EntitlementRepositoryImpl(settings, client, dispatcher, time)
        repo.refresh()
        assertThat(client.refreshCalls).isEqualTo(1)
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun markPurchasePendingStoresToday() = runTest {
        val settings = FakeSettings()
        val repo = EntitlementRepositoryImpl(settings, FakeClient(), dispatcher, time)
        repo.markPurchasePending()
        assertThat(settings.app.value.purchasePendingSince).isEqualTo(today)
    }

    @Test
    fun refreshClearsThePendingMarkOnceEntitled() = runTest {
        val settings = FakeSettings()
        settings.app.value = settings.app.value.copy(purchasePendingSince = today - 2)
        val repo = EntitlementRepositoryImpl(settings, FakeClient(entitled = true), dispatcher, time)
        repo.refresh()
        assertThat(settings.app.value.proUnlocked).isTrue()
        assertThat(settings.app.value.purchasePendingSince).isEqualTo(0L)
    }

    @Test
    fun startCheckStaysOfflineForFreeUsersWithoutPendingPurchase() = runTest {
        val client = FakeClient(entitled = true)
        val repo = EntitlementRepositoryImpl(FakeSettings(), client, dispatcher, time)
        repo.refreshOnAppStart()
        assertThat(client.refreshCalls).isEqualTo(0)
    }

    @Test
    fun startCheckRelocksProAfterARefund() = runTest {
        val settings = FakeSettings(initialPro = true)
        val client = FakeClient(entitled = false)
        val repo = EntitlementRepositoryImpl(settings, client, dispatcher, time)
        repo.refreshOnAppStart()
        assertThat(client.refreshCalls).isEqualTo(1)
        assertThat(settings.app.value.proUnlocked).isFalse()
    }

    @Test
    fun startCheckUnlocksAPendingPurchaseThatCompleted() = runTest {
        val settings = FakeSettings()
        settings.app.value = settings.app.value.copy(purchasePendingSince = today - 14)
        val client = FakeClient(entitled = true)
        val repo = EntitlementRepositoryImpl(settings, client, dispatcher, time)
        repo.refreshOnAppStart()
        assertThat(client.refreshCalls).isEqualTo(1)
        assertThat(settings.app.value.proUnlocked).isTrue()
    }

    @Test
    fun startCheckDropsAPendingMarkOlderThanTwoWeeks() = runTest {
        val settings = FakeSettings()
        settings.app.value = settings.app.value.copy(purchasePendingSince = today - 15)
        val client = FakeClient(entitled = true)
        val repo = EntitlementRepositoryImpl(settings, client, dispatcher, time)
        repo.refreshOnAppStart()
        assertThat(client.refreshCalls).isEqualTo(0)
        assertThat(settings.app.value.purchasePendingSince).isEqualTo(0L)
    }
}
