package com.baer.memolio.feature.manage.playlist

import app.cash.turbine.test
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.model.Album
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

class PlaylistViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeSettingsRepository : SettingsRepository {
        val config = MutableStateFlow(PlaylistConfig())
        override val playlistConfig: Flow<PlaylistConfig> = config
        override suspend fun setActiveAlbumIds(ids: Set<String>) { config.value = config.value.copy(activeAlbumIds = ids) }
        override suspend fun setShuffle(value: Boolean) { config.value = config.value.copy(shuffle = value) }
        override suspend fun setIntervalSeconds(value: Int) { config.value = config.value.copy(intervalSeconds = value) }
        override suspend fun setTransition(value: TransitionStyle) { config.value = config.value.copy(transition = value) }
        override suspend fun setFitMode(value: FitMode) { config.value = config.value.copy(fitMode = value) }
        override suspend fun setShowClock(value: Boolean) { config.value = config.value.copy(showClock = value) }
        override suspend fun setShowDate(value: Boolean) { config.value = config.value.copy(showDate = value) }
        override suspend fun setShowCaption(value: Boolean) { config.value = config.value.copy(showCaption = value) }
        // --- stubbed members (not exercised by playlist tests) ---
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

    private class FakeAlbumRepository(albums: List<Album>) : AlbumRepository {
        val flow = MutableStateFlow(albums)
        override fun observeAlbums(): Flow<List<Album>> = flow
        override suspend fun upsert(album: Album) {}
        override suspend fun delete(id: String) {}
    }

    private fun vm(
        settings: FakeSettingsRepository = FakeSettingsRepository(),
        albums: FakeAlbumRepository = FakeAlbumRepository(listOf(Album("a1", "A1", null, 0L, 0)))
    ) = PlaylistViewModel(settings, albums) to settings

    @Test
    fun stateReflectsCurrentConfigAndAlbums() = runTest {
        val (model, _) = vm()
        model.state.test {
            // tolerate conflation of the initial default state under UnconfinedTestDispatcher
            var s = awaitItem()
            if (s.allAlbums.isEmpty()) s = awaitItem()
            assertThat(s.intervalSeconds).isEqualTo(30)
            assertThat(s.shuffle).isTrue()
            assertThat(s.fitMode).isEqualTo(FitMode.BLURRED_FILL)
            assertThat(s.transition).isEqualTo(TransitionStyle.KEN_BURNS_CROSSFADE)
            assertThat(s.allAlbums.map { it.id }).containsExactly("a1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleAlbumPersistsActiveSet() = runTest {
        val (model, settings) = vm()
        model.toggleAlbum("a1")
        assertThat(settings.config.value.activeAlbumIds).containsExactly("a1")
        model.toggleAlbum("a1")
        assertThat(settings.config.value.activeAlbumIds).isEmpty()
    }

    @Test
    fun settersPersistEveryPlaylistField() = runTest {
        val (model, settings) = vm()
        model.setShuffle(false)
        model.setInterval(90)
        model.setTransition(TransitionStyle.CROSSFADE)
        model.setFitMode(FitMode.CROP)
        model.setShowClock(false)
        model.setShowDate(false)
        model.setShowCaption(false)

        val c = settings.config.value
        assertThat(c.shuffle).isFalse()
        assertThat(c.intervalSeconds).isEqualTo(90)
        assertThat(c.transition).isEqualTo(TransitionStyle.CROSSFADE)
        assertThat(c.fitMode).isEqualTo(FitMode.CROP)
        assertThat(c.showClock).isFalse()
        assertThat(c.showDate).isFalse()
        assertThat(c.showCaption).isFalse()
    }
}
