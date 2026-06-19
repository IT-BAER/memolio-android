package com.baer.memolio.feature.manage.wallpaper

import app.cash.turbine.test
import com.baer.memolio.core.datastore.AppSettings
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

class WallpaperViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeSettings : SettingsRepository {
        val app = MutableStateFlow(AppSettings())
        var lastWallpaper: String? = null
        override val appSettings: Flow<AppSettings> = app
        override suspend fun setWallpaperId(value: String) { lastWallpaper = value; app.value = app.value.copy(wallpaperId = value) }
        override val playlistConfig: Flow<PlaylistConfig> = MutableStateFlow(PlaylistConfig())
        override suspend fun setActiveAlbumIds(ids: Set<String>) {}
        override suspend fun setShuffle(value: Boolean) {}
        override suspend fun setIntervalSeconds(value: Int) {}
        override suspend fun setTransition(value: TransitionStyle) {}
        override suspend fun setFitMode(value: FitMode) {}
        override suspend fun setShowClock(value: Boolean) {}
        override suspend fun setShowDate(value: Boolean) {}
        override suspend fun setShowCaption(value: Boolean) {}
        override suspend fun setUploadToken(token: String) {}
        override suspend fun setServerPort(port: Int) {}
        override suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int) {}
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
    fun exposesAvailableWallpapersAndSelected() = runTest {
        val settings = FakeSettings().apply { app.value = AppSettings(wallpaperId = "default") }
        val vm = WallpaperViewModel(settings)
        vm.state.test {
            var s = awaitItem()
            assertThat(s.available).contains("default")
            assertThat(s.selectedId).isEqualTo("default")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectPersistsWallpaperId() = runTest {
        val settings = FakeSettings()
        val vm = WallpaperViewModel(settings)
        vm.select("default")
        assertThat(settings.lastWallpaper).isEqualTo("default")
    }
}
