package com.baer.memolio.feature.manage.addphotos

import app.cash.turbine.test
import com.baer.memolio.core.datastore.AppSettings
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

class AddPhotosViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeUploadUrlProvider(initial: String?) : UploadUrlProvider {
        val flow = MutableStateFlow(initial)
        override val uploadUrl: Flow<String?> = flow
    }

    private class FakeSettings : SettingsRepository {
        var rotated = 0
        override val playlistConfig: Flow<PlaylistConfig> = MutableStateFlow(PlaylistConfig())
        override suspend fun setActiveAlbumIds(ids: Set<String>) {}
        override suspend fun setShuffle(value: Boolean) {}
        override suspend fun setIntervalSeconds(value: Int) {}
        override suspend fun setTransition(value: TransitionStyle) {}
        override suspend fun setFitMode(value: FitMode) {}
        override suspend fun setShowClock(value: Boolean) {}
        override suspend fun setShowDate(value: Boolean) {}
        override suspend fun setShowCaption(value: Boolean) {}
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
        override suspend fun rotateToken(): String { rotated++; return "newtoken" }
        override suspend fun ensureToken(): String = "token"
    }

    @Test
    fun exposesUploadUrlFromProvider() = runTest {
        val vm = AddPhotosViewModel(FakeUploadUrlProvider("http://192.168.1.5:8080/?t=abc"), FakeSettings())
        vm.state.test {
            var s = awaitItem()
            if (s.uploadUrl == null) s = awaitItem()
            assertThat(s.uploadUrl).isEqualTo("http://192.168.1.5:8080/?t=abc")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nullUrlMeansServerNotReady() = runTest {
        val vm = AddPhotosViewModel(FakeUploadUrlProvider(null), FakeSettings())
        vm.state.test {
            assertThat(awaitItem().uploadUrl).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun rotateTokenCallsSettings() = runTest {
        val settings = FakeSettings()
        val vm = AddPhotosViewModel(FakeUploadUrlProvider("http://x/?t=old"), settings)

        vm.rotateToken()

        assertThat(settings.rotated).isEqualTo(1)
    }
}
