package com.baer.memolio.feature.manage.storage

import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.TransitionStyle
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.storage.FileStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import app.cash.turbine.test
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class StorageViewModelTest {

    @get:Rule val tmp = TemporaryFolder()
    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakePhotoRepo : PhotoRepository {
        val trash = MutableStateFlow<List<Photo>>(emptyList())
        var lastRestore: String? = null
        var lastPurgeThreshold: Long? = null
        override fun observeTrash(): Flow<List<Photo>> = trash
        override fun observePhotos(albumId: String): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override suspend fun isDuplicate(contentHash: String) = false
        override suspend fun add(
            id: String, originalPath: String, displayCachePath: String, thumbPath: String,
            contentHash: String, width: Int, height: Int, orientation: Int,
            caption: String?, albumId: String, sourceDevice: String?, addedAt: Long
        ) {}
        override suspend fun softDelete(id: String, now: Long) {}
        override suspend fun restore(id: String) { lastRestore = id }
        override suspend fun purgeTrashOlderThan(threshold: Long): Int { lastPurgeThreshold = threshold; return 1 }
        override suspend fun moveToAlbum(id: String, albumId: String) {}
        override suspend fun setFavorite(id: String, favorite: Boolean) {}
        override suspend fun setCaption(id: String, caption: String?) {}
        override suspend fun reorder(orderedIds: List<String>) {}
    }

    private class FakeSettings : SettingsRepository {
        val app = MutableStateFlow(AppSettings())
        var lastAutoCleanup: Boolean? = null
        override val appSettings: Flow<AppSettings> = app
        override suspend fun setAutoCleanup(value: Boolean) { lastAutoCleanup = value; app.value = app.value.copy(autoCleanup = value) }
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
        override suspend fun setWallpaperId(value: String) {}
        override suspend fun setOnboardingComplete(value: Boolean) {}
        override suspend fun setProUnlocked(value: Boolean) {}
        override suspend fun rotateToken(): String = ""
        override suspend fun ensureToken(): String = ""
    }

    private fun photo(id: String) = Photo(
        id = id, originalPath = "", displayCachePath = "", thumbPath = "", contentHash = id,
        width = 1, height = 1, orientation = 0, caption = null, albumId = "a", favorite = false,
        sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = 1L
    )

    private fun newVm(repo: FakePhotoRepo, settings: FakeSettings): StorageViewModel {
        val storage = FileStorage(tmp.root)
        storage.writeOriginal("p1", "jpg") { it.write(byteArrayOf(1, 2, 3)) }
        return StorageViewModel(repo, settings, storage, dispatcher) { 1000L }
    }

    @Test
    fun stateExposesUsedBytesTrashAndAutoCleanup() = runTest {
        val repo = FakePhotoRepo().apply { trash.value = listOf(photo("p1")) }
        val settings = FakeSettings().apply { app.value = AppSettings(autoCleanup = true) }
        val vm = newVm(repo, settings)
        vm.state.test {
            var s = awaitItem()
            while (s.trash.isEmpty() || s.usedBytes == 0L) s = awaitItem()
            assertThat(s.usedBytes).isEqualTo(3L)
            assertThat(s.trash.map { it.id }).containsExactly("p1")
            assertThat(s.autoCleanup).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun restoreDelegatesToRepository() = runTest {
        val repo = FakePhotoRepo()
        val vm = newVm(repo, FakeSettings())
        vm.restore("p1")
        assertThat(repo.lastRestore).isEqualTo("p1")
    }

    @Test
    fun emptyTrashPurgesEverything() = runTest {
        val repo = FakePhotoRepo()
        val vm = newVm(repo, FakeSettings())
        vm.emptyTrash()
        assertThat(repo.lastPurgeThreshold).isEqualTo(Long.MAX_VALUE)
    }

    @Test
    fun toggleAutoCleanupPersists() = runTest {
        val settings = FakeSettings()
        val vm = newVm(FakePhotoRepo(), settings)
        vm.setAutoCleanup(true)
        assertThat(settings.lastAutoCleanup).isTrue()
    }
}
