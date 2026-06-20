package com.baer.memolio.feature.frame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.PlaylistConfig
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.di.DefaultDispatcher
import com.baer.memolio.core.di.ShuffleSeed
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.time.FrameClock
import com.baer.memolio.core.time.FrameTick
import com.baer.memolio.core.time.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Combines the active playlist config + the photos in the active albums + the clock
 * into a [FrameUiState]. Advances the slideshow on PlaylistConfig.intervalSeconds via
 * an internal ticker. All timing runs on the collector's dispatcher (the injected
 * [defaultDispatcher] in tests, where `Dispatchers.setMain` makes it virtual-time), so
 * tests use virtual time. No real clock is read here — [timeProvider] is injected.
 *
 * [shuffleSeed] is injected (defaulting to the wall clock in production via the
 * `@ShuffleSeed` provider) so shuffle order is reproducible in tests.
 *
 * IMPORTANT: the advance ticker lives INSIDE the [uiState] flow (a `flow { while … }`
 * under `flatMapLatest`), so it only runs while [uiState] is subscribed and is torn
 * down by `WhileSubscribed`. It is never launched as a free-running coroutine in
 * `viewModelScope` — doing so leaks a `while(true){delay()}` that can never go idle.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FrameViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    photoRepository: PhotoRepository,
    timeProvider: TimeProvider,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ShuffleSeed private val shuffleSeed: Long
) : ViewModel() {

    private val clock = FrameClock(timeProvider, defaultDispatcher)

    // The hero clock stays 24h by design (giant thin numerals, no AM/PM); digits are
    // locale-neutral. The date line, however, follows the locale's own field order and
    // grammar (e.g. de "Samstag, 20. Juni", ru genitive month, zh "6月20日星期六") via the
    // best-fit skeleton, falling back to the day-month pattern if a locale yields an
    // expression DateTimeFormatter can't parse.
    private val locale: Locale = Locale.getDefault()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dateFormatter = runCatching {
        DateTimeFormatter.ofPattern(
            android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM"),
            locale
        )
    }.getOrElse { DateTimeFormatter.ofPattern("EEEE, d MMMM", locale) }

    private val config: Flow<PlaylistConfig> = settingsRepository.playlistConfig

    /** Selected wallpaper id (applies to the idle home); defaults to the live vector. */
    private val wallpaperId: Flow<String> =
        settingsRepository.appSettings.map { it.wallpaperId }.distinctUntilChanged()

    /**
     * Photos for the current active album set, re-querying when the set changes. An
     * empty set means "no album filter" → show the whole live pool, which is the
     * default free-tier source (uploads land in the single pool, no albums selected).
     */
    private val photos: Flow<List<Photo>> =
        config.map { it.activeAlbumIds }
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) photoRepository.observeSlideshowPool()
                else photoRepository.observeSlideshowInAlbums(ids)
            }

    /**
     * The slideshow cursor. Rebuilt from scratch (index 0) whenever the photo id-set,
     * shuffle flag, or interval changes; then advanced every `intervalSeconds`. The
     * `while (true)` ticker is scoped to this flow's collection, so `WhileSubscribed`
     * cancels it when nothing observes the frame.
     */
    private val cursor: Flow<FramePlaylist> =
        combine(
            photos.map { list -> list.map(Photo::id) }.distinctUntilChanged(),
            config.map { it.shuffle }.distinctUntilChanged(),
            config.map { it.intervalSeconds }.distinctUntilChanged()
        ) { ids, shuffle, interval -> Triple(ids, shuffle, interval) }
            .flatMapLatest { (ids, shuffle, interval) ->
                flow {
                    var current = FramePlaylist.create(ids, shuffle, shuffleSeed)
                    emit(current)
                    val millis = interval.coerceAtLeast(1) * 1_000L
                    while (true) {
                        delay(millis)
                        current = current.advanced()
                        emit(current)
                    }
                }
            }

    val uiState: StateFlow<FrameUiState> =
        combine(
            cursor,
            photos,
            config,
            clock.now,
            wallpaperId
        ) { playlistCursor, livePhotos, playlistConfig, tick, wallpaper ->
            buildState(playlistCursor, livePhotos, playlistConfig, tick, wallpaper)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FrameUiState.Loading
        )

    private fun buildState(
        cursor: FramePlaylist,
        livePhotos: List<Photo>,
        config: PlaylistConfig,
        tick: FrameTick,
        wallpaperId: String
    ): FrameUiState {
        val time = tick.time.format(timeFormatter)
        val date = tick.date.format(dateFormatter)
        val byId = livePhotos.associateBy { it.id }
        val current = cursor.currentId?.let(byId::get)
        val next = cursor.nextId?.let(byId::get)
        return if (current == null || next == null) {
            FrameUiState.Idle(
                time = time,
                date = date,
                driftPhase = tick.dayFraction,
                showClock = config.showClock,
                showDate = config.showDate,
                wallpaperId = wallpaperId
            )
        } else {
            FrameUiState.Slideshow(
                currentPhoto = current,
                nextPhoto = next,
                position = cursor.position,
                total = cursor.size,
                time = time,
                date = date,
                showClock = config.showClock,
                showDate = config.showDate,
                showCaption = config.showCaption
            )
        }
    }
}
