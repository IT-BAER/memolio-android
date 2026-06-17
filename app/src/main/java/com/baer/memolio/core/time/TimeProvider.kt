package com.baer.memolio.core.time

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * The ONLY place the real wall clock is read. Inject it everywhere time is needed
 * so logic stays deterministic in tests (the spec/CLAUDE.md forbid calling the real
 * clock directly in tested code).
 */
interface TimeProvider {
    fun now(): LocalDateTime
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

/** One clock sample. [dayFraction] (0f..1f) drives burn-in drift of the idle clock. */
data class FrameTick(
    val dateTime: LocalDateTime
) {
    val time: LocalTime get() = dateTime.toLocalTime()
    val date: LocalDate get() = dateTime.toLocalDate()
    val dayFraction: Float
        get() = (time.toSecondOfDay().toFloat() / SECONDS_PER_DAY)

    private companion object {
        const val SECONDS_PER_DAY = 86_400f
    }
}

/**
 * Emits a [FrameTick] immediately, then once per minute. Runs on the injected
 * [tickDispatcher] so tests drive it with virtual time via `StandardTestDispatcher`.
 * Production binds [com.baer.memolio.core.di.TimeModule]'s @DefaultDispatcher.
 */
class FrameClock(
    private val timeProvider: TimeProvider,
    private val tickDispatcher: CoroutineDispatcher
) {
    val now: Flow<FrameTick> = flow {
        while (true) {
            emit(FrameTick(timeProvider.now()))
            delay(TICK_MILLIS)
        }
    }.flowOn(tickDispatcher)

    private companion object {
        const val TICK_MILLIS = 60_000L
    }
}
