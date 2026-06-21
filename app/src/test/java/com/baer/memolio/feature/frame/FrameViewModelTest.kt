package com.baer.memolio.feature.frame

import app.cash.turbine.test
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.time.TimeProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class FrameViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val configFlow = MutableStateFlow(PlaylistConfig())
    private val photosFlow = MutableStateFlow<List<Photo>>(emptyList())
    private var observedAlbums: Set<String> = emptySet()

    private val fixedTime = object : TimeProvider {
        override fun now(): LocalDateTime = LocalDateTime.of(2026, 6, 16, 14, 32, 0)
    }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun photo(id: String, caption: String? = null) = Photo(
        id = id, originalPath = "/o/$id.jpg", displayCachePath = "/d/$id.jpg",
        thumbPath = "/t/$id.jpg", contentHash = id, width = 100, height = 100,
        orientation = 0, caption = caption, albumId = "a1", favorite = false,
        sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = null
    )

    private fun newViewModel(
        wallpaperRepo: FakeWallpaperRepository = FakeWallpaperRepository()
    ): FrameViewModel {
        val fakeSettings = FakeSettings(configFlow)
        val fakePhotos = FakePhotos(photosFlow) { observedAlbums = it }
        return FrameViewModel(
            settingsRepository = fakeSettings,
            photoRepository = fakePhotos,
            wallpaperRepository = wallpaperRepo,
            timeProvider = fixedTime,
            defaultDispatcher = dispatcher,
            shuffleSeed = 1L
        )
    }

    @Test
    fun startsIdleWhenNoPhotos() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1"))
        val vm = newViewModel()
        vm.uiState.test {
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Loading::class.java)
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun showsSlideshowWhenPhotosExist() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1"), shuffle = false)
        photosFlow.value = listOf(photo("p1"), photo("p2"), photo("p3"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1) // Loading
            runCurrent()
            val s = awaitItem() as FrameUiState.Slideshow
            assertThat(s.currentPhoto.id).isEqualTo("p1")
            assertThat(s.nextPhoto.id).isEqualTo("p2")
            assertThat(s.position).isEqualTo(1)
            assertThat(s.total).isEqualTo(3)
            assertThat(s.time).isEqualTo("14:32")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun advancesOnIntervalAndWraps() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(
            activeAlbumIds = setOf("a1"), shuffle = false, intervalSeconds = 30
        )
        photosFlow.value = listOf(photo("p1"), photo("p2"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertThat((awaitItem() as FrameUiState.Slideshow).currentPhoto.id).isEqualTo("p1")
            advanceTimeBy(30_001)
            assertThat((awaitItem() as FrameUiState.Slideshow).currentPhoto.id).isEqualTo("p2")
            advanceTimeBy(30_001)
            assertThat((awaitItem() as FrameUiState.Slideshow).currentPhoto.id).isEqualTo("p1") // wrapped
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fallsBackToIdleWhenLastPhotoRemoved() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1"), shuffle = false)
        photosFlow.value = listOf(photo("p1"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Slideshow::class.java)
            photosFlow.value = emptyList()
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun transitionsIdleToSlideshowWhenPhotoAdded() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1"), shuffle = false)
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Idle::class.java)
            photosFlow.value = listOf(photo("p1"))
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Slideshow::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shuffleYieldsDeterministicOrderForFixedSeed() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1"), shuffle = true)
        photosFlow.value = (1..6).map { photo("p$it") }
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            // Deterministic permutation for seed = 1L over [p1..p6].
            val expected = (1..6).map { "p$it" }.shuffled(kotlin.random.Random(1L))
            assertThat((awaitItem() as FrameUiState.Slideshow).currentPhoto.id)
                .isEqualTo(expected.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun togglingActiveAlbumsUpdatesObservedSet() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1"), shuffle = false)
        photosFlow.value = listOf(photo("p1"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Slideshow::class.java)
            assertThat(observedAlbums).containsExactly("a1")
            configFlow.value = PlaylistConfig(activeAlbumIds = setOf("a1", "a2"), shuffle = false)
            runCurrent()
            assertThat(observedAlbums).containsExactly("a1", "a2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun overlayTogglesPropagateFromConfig() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(
            activeAlbumIds = setOf("a1"), shuffle = false,
            showClock = false, showDate = false, showCaption = false
        )
        photosFlow.value = listOf(photo("p1"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            val s = awaitItem() as FrameUiState.Slideshow
            assertThat(s.showClock).isFalse()
            assertThat(s.showDate).isFalse()
            assertThat(s.showCaption).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptyAlbumSelectionWithEmptyPoolStaysIdle() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(activeAlbumIds = emptySet())
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertThat(awaitItem()).isInstanceOf(FrameUiState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptyAlbumSelectionShowsWholePool() = runTest(dispatcher) {
        // Default free-tier config: no album filter selected. Photos uploaded to the
        // single pool MUST still drive the slideshow, not leave the frame idle.
        configFlow.value = PlaylistConfig(activeAlbumIds = emptySet(), shuffle = false)
        photosFlow.value = listOf(photo("p1"), photo("p2"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            val s = awaitItem() as FrameUiState.Slideshow
            assertThat(s.currentPhoto.id).isEqualTo("p1")
            assertThat(s.total).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fitModePropagatesFromConfig() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(
            activeAlbumIds = setOf("a1"), shuffle = false, fitMode = FitMode.CROP
        )
        photosFlow.value = listOf(photo("p1"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1) // Loading
            runCurrent()
            val s = awaitItem() as FrameUiState.Slideshow
            assertThat(s.fitMode).isEqualTo(FitMode.CROP)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clockStyleAndTimePartsPropagate() = runTest(dispatcher) {
        configFlow.value = PlaylistConfig(
            activeAlbumIds = setOf("a1"), shuffle = false,
            clockStyle = com.baer.memolio.core.datastore.ClockStyle.ANALOG,
            clockOpacity = 0.6f, clockScale = 1.25f
        )
        photosFlow.value = listOf(photo("p1"))
        val vm = newViewModel()
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            val s = awaitItem() as FrameUiState.Slideshow
            assertThat(s.clockStyle).isEqualTo(com.baer.memolio.core.datastore.ClockStyle.ANALOG)
            assertThat(s.hour).isEqualTo(14)
            assertThat(s.minute).isEqualTo(32)
            assertThat(s.clockOpacity).isEqualTo(0.6f)
            assertThat(s.clockScale).isEqualTo(1.25f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

// --- Fakes (same file). Per shared-contract addendum A, fakes implement the real
// repository interfaces directly; no separate read-only seams exist. ---

private class FakeSettings(
    private val flow: MutableStateFlow<PlaylistConfig>
) : com.baer.memolio.core.datastore.SettingsRepository {
    override val playlistConfig: Flow<PlaylistConfig> = flow

    // Unused by FrameViewModel; not exercised in these tests.
    override val appSettings: Flow<com.baer.memolio.core.datastore.AppSettings> =
        flowOf(com.baer.memolio.core.datastore.AppSettings())
    override suspend fun setActiveAlbumIds(ids: Set<String>) = Unit
    override suspend fun setShuffle(value: Boolean) = Unit
    override suspend fun setIntervalSeconds(value: Int) = Unit
    override suspend fun setTransition(value: com.baer.memolio.core.datastore.TransitionStyle) = Unit
    override suspend fun setFitMode(value: com.baer.memolio.core.datastore.FitMode) = Unit
    override suspend fun setShowClock(value: Boolean) = Unit
    override suspend fun setShowDate(value: Boolean) = Unit
    override suspend fun setShowCaption(value: Boolean) = Unit
    override suspend fun setClockStyle(value: com.baer.memolio.core.datastore.ClockStyle) = Unit
    override suspend fun setClockOpacity(value: Float) = Unit
    override suspend fun setClockScale(value: Float) = Unit
    override suspend fun setUploadToken(token: String) = Unit
    override suspend fun setServerPort(port: Int) = Unit
    override suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int) = Unit
    override suspend fun setKioskEnabled(value: Boolean) = Unit
    override suspend fun setHomeAppEnabled(value: Boolean) = Unit
    override suspend fun setAutostartEnabled(value: Boolean) = Unit
    override suspend fun setAmbientDimming(value: Boolean) = Unit
    override suspend fun setBrightness(value: Float) = Unit
    override suspend fun setAutoCleanup(value: Boolean) = Unit
    override suspend fun setWallpaperId(value: String) = Unit
    override suspend fun setOnboardingComplete(value: Boolean) = Unit
    override suspend fun setProUnlocked(value: Boolean) = Unit
    override suspend fun rotateToken(): String = ""
    override suspend fun ensureToken(): String = ""
}

private class FakeWallpaperRepository(
    var customPath: String? = null
) : com.baer.memolio.core.data.WallpaperRepository {
    override suspend fun importCustom(uri: android.net.Uri): String = "custom"
    override fun customWallpaperPath(): String? = customPath
    override suspend fun clearCustom() { customPath = null }
}

private class FakePhotos(
    private val flow: MutableStateFlow<List<Photo>>,
    private val onObserve: (Set<String>) -> Unit
) : PhotoRepository {
    override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> {
        onObserve(albumIds)
        return if (albumIds.isEmpty()) flowOf(emptyList()) else flow
    }

    override fun observeAllLivePhotos(): Flow<List<Photo>> = flow

    // FrameViewModel now reads the slideshow-filtered sources; mirror the live ones so the
    // fake's backing flow drives the same behavior (DB-level inPlaylist filtering is covered
    // by the DAO/repository tests).
    override fun observeSlideshowPool(): Flow<List<Photo>> = flow

    override fun observeSlideshowInAlbums(albumIds: Set<String>): Flow<List<Photo>> {
        onObserve(albumIds)
        return if (albumIds.isEmpty()) flowOf(emptyList()) else flow
    }

    override suspend fun setInPlaylist(id: String, inPlaylist: Boolean) = Unit

    // Unused by FrameViewModel; not exercised in these tests.
    override fun observePhotos(albumId: String): Flow<List<Photo>> = flowOf(emptyList())
    override fun observeTrash(): Flow<List<Photo>> = flowOf(emptyList())
    override suspend fun isDuplicate(contentHash: String): Boolean = false
    override suspend fun add(
        id: String, originalPath: String, displayCachePath: String, thumbPath: String,
        contentHash: String, width: Int, height: Int, orientation: Int, caption: String?,
        albumId: String, sourceDevice: String?, addedAt: Long
    ) = Unit
    override suspend fun softDelete(id: String, now: Long) = Unit
    override suspend fun restore(id: String) = Unit
    override suspend fun purgeTrashOlderThan(threshold: Long): Int = 0
    override suspend fun moveToAlbum(id: String, albumId: String) = Unit
    override suspend fun setFavorite(id: String, favorite: Boolean) = Unit
    override suspend fun setCaption(id: String, caption: String?) = Unit
    override suspend fun reorder(orderedIds: List<String>) = Unit
    override suspend fun setFocalPoint(id: String, x: Float, y: Float) = Unit
}
