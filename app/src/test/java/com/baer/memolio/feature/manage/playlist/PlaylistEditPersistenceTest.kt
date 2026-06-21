package com.baer.memolio.feature.manage.playlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.model.Album
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Manage is a landscape-tablet experience; render at tablet width so the Playlist
// two-pane (AdaptiveTwoPane) lays out side-by-side as it does on the device.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-xhdpi")
class PlaylistEditPersistenceTest {

    @get:Rule val composeRule = createComposeRule()

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: android.app.Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

    private class FakeSettings : SettingsRepository {
        val config = MutableStateFlow(PlaylistConfig())
        override val playlistConfig: Flow<PlaylistConfig> = config
        override suspend fun setActiveAlbumIds(ids: Set<String>) { config.value = config.value.copy(activeAlbumIds = ids) }
        override suspend fun setShuffle(value: Boolean) { config.value = config.value.copy(shuffle = value) }
        override suspend fun setIntervalSeconds(value: Int) { config.value = config.value.copy(intervalSeconds = value) }
        override suspend fun setTransition(value: TransitionStyle) {}
        override suspend fun setFitMode(value: FitMode) {}
        override suspend fun setShowClock(value: Boolean) {}
        override suspend fun setShowDate(value: Boolean) {}
        override suspend fun setShowCaption(value: Boolean) {}
        override suspend fun setClockStyle(value: ClockStyle) { config.value = config.value.copy(clockStyle = value) }
        override suspend fun setClockOpacity(value: Float) { config.value = config.value.copy(clockOpacity = value) }
        override suspend fun setClockScale(value: Float) { config.value = config.value.copy(clockScale = value) }
        override val appSettings: Flow<AppSettings> = MutableStateFlow(AppSettings())
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
        override suspend fun setProUnlocked(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    private class FakeAlbumRepo : AlbumRepository {
        override fun observeAlbums(): Flow<List<Album>> = MutableStateFlow(listOf(Album("a1", "A1", null, 0L, 0)))
        override suspend fun upsert(album: Album) {}
        override suspend fun delete(id: String) {}
    }

    @Test
    fun playlistScreenRendersAlbumAndControlRows() {
        val settings = FakeSettings()
        val vm = PlaylistViewModel(settings, FakeAlbumRepo(), FakeEntitlement(true))
        composeRule.setContent { PlaylistScreen(viewModel = vm) }
        composeRule.onNodeWithText("A1").assertIsDisplayed()
        composeRule.onNodeWithText("Shuffle").assertIsDisplayed()
        composeRule.onNodeWithText("Show clock").assertIsDisplayed()
    }

    @Test
    fun shuffleToggleWritesThroughToSettings() {
        val settings = FakeSettings()
        val vm = PlaylistViewModel(settings, FakeAlbumRepo(), FakeEntitlement(true))
        composeRule.setContent { PlaylistScreen(viewModel = vm) }

        vm.setShuffle(false)
        composeRule.waitForIdle()
        assertThat(settings.config.value.shuffle).isFalse()
    }

    @Test
    fun analogClockSwitchWritesThroughToSettings() {
        val settings = FakeSettings()
        val vm = PlaylistViewModel(settings, FakeAlbumRepo(), FakeEntitlement(true))
        composeRule.setContent { PlaylistScreen(viewModel = vm) }

        vm.setAnalogClock(true)
        composeRule.waitForIdle()
        assertThat(settings.config.value.clockStyle)
            .isEqualTo(com.baer.memolio.core.datastore.ClockStyle.ANALOG)
    }
}
