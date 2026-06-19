package com.baer.memolio.appliance

import com.baer.memolio.core.datastore.AppSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

/**
 * Drives a [sleeping] StateFlow from a [ticks] Flow + [appSettings], using pure
 * [SleepWindow]. A manual [wake] (tap) forces awake; the next tick re-evaluates the
 * schedule. The server keeps running while asleep; this only controls the display.
 */
class SleepDriver(
    ticks: Flow<Unit>,
    appSettings: Flow<AppSettings>,
    private val time: TimeProvider,
    scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher
) {
    private val _sleeping = MutableStateFlow(false)
    val sleeping: StateFlow<Boolean> = _sleeping.asStateFlow()

    @Volatile private var manualWakeActive = false

    init {
        combine(ticks, appSettings) { _, settings -> settings }
            .onEach { settings -> evaluate(settings) }
            .launchIn(CoroutineScope(scope.coroutineContext + dispatcher))
    }

    private suspend fun evaluate(settings: AppSettings) = withContext(dispatcher) {
        if (manualWakeActive) {
            manualWakeActive = false
            _sleeping.value = false
            return@withContext
        }
        _sleeping.value = SleepWindow.shouldSleep(
            nowMinutes = time.nowMinutesSinceMidnight(),
            enabled = settings.sleepEnabled,
            startMinutes = settings.sleepStartMinutes,
            endMinutes = settings.sleepEndMinutes
        )
    }

    /** Tap-to-wake: force awake now; schedule resumes on the next tick. */
    fun wake() {
        manualWakeActive = true
        _sleeping.value = false
    }
}
