package com.baer.memolio.feature.manage.wallpaper

import android.net.Uri
import app.cash.turbine.test
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import com.baer.memolio.core.data.WallpaperRepository
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.ui.CUSTOM_WALLPAPER_ID
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric so android.net.Uri.parse works in pickCustom tests.
@RunWith(RobolectricTestRunner::class)
class WallpaperViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: android.app.Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

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
        override suspend fun setOnboardingComplete(value: Boolean) {}
        override suspend fun setProUnlocked(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    private class FakeWallpaperRepository(
        var customPath: String? = null
    ) : WallpaperRepository {
        var importedUri: Uri? = null
        override suspend fun importCustom(uri: Uri): String {
            importedUri = uri
            return "custom"
        }
        override fun customWallpaperPath(): String? = customPath
        override suspend fun clearCustom() { customPath = null }
    }

    @Test
    fun exposesAvailableWallpapersAndSelected() = runTest {
        val settings = FakeSettings().apply { app.value = AppSettings(wallpaperId = "default") }
        val vm = WallpaperViewModel(settings, FakeEntitlement(true), FakeWallpaperRepository())
        vm.state.test {
            val s = awaitItem()
            assertThat(s.available).contains("default")
            assertThat(s.selectedId).isEqualTo("default")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectPersistsWallpaperId() = runTest {
        val settings = FakeSettings()
        val vm = WallpaperViewModel(settings, FakeEntitlement(true), FakeWallpaperRepository())
        vm.select("default")
        assertThat(settings.lastWallpaper).isEqualTo("default")
    }

    @Test
    fun pickCustomImportsAndSelectsCustomWhenPro() = runTest {
        val settings = FakeSettings()
        val repo = FakeWallpaperRepository()
        val vm = WallpaperViewModel(settings, FakeEntitlement(true), repo)
        val uri = Uri.parse("content://media/test/1")
        vm.pickCustom(uri)
        assertThat(repo.importedUri).isEqualTo(uri)
        assertThat(settings.lastWallpaper).isEqualTo(CUSTOM_WALLPAPER_ID)
    }

    @Test
    fun pickCustomIsNoOpWhenNotPro() = runTest {
        val settings = FakeSettings()
        val repo = FakeWallpaperRepository()
        val vm = WallpaperViewModel(settings, FakeEntitlement(false), repo)
        val uri = Uri.parse("content://media/test/1")
        vm.pickCustom(uri)
        assertThat(repo.importedUri).isNull()
        assertThat(settings.lastWallpaper).isNull()
    }

    @Test
    fun stateExposesCustomPathFromRepo() = runTest {
        val settings = FakeSettings()
        val repo = FakeWallpaperRepository(customPath = "/x/custom.jpg")
        val vm = WallpaperViewModel(settings, FakeEntitlement(true), repo)
        vm.state.test {
            val s = awaitItem()
            assertThat(s.customPath).isEqualTo("/x/custom.jpg")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
