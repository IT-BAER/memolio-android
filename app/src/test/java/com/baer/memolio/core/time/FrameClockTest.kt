package com.baer.memolio.core.time

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FrameClockTest {

    /** Deterministic provider: time advances by `step` each `now()` call. */
    private class FakeTimeProvider(
        start: LocalDateTime,
        private val stepSeconds: Long
    ) : TimeProvider {
        private var current = start
        override fun now(): LocalDateTime {
            val snapshot = current
            current = current.plusSeconds(stepSeconds)
            return snapshot
        }
    }

    @Test
    fun emitsImmediatelyThenOnEachTick() = runTest {
        val provider = FakeTimeProvider(
            LocalDateTime.of(2026, 6, 16, 14, 32, 0),
            stepSeconds = 60
        )
        val clock = FrameClock(provider, StandardTestDispatcher(testScheduler))

        clock.now.test {
            // first emission is immediate
            assertThat(awaitItem().time).isEqualTo(LocalTime.of(14, 32))
            advanceTimeBy(1_000)
            assertThat(awaitItem().time).isEqualTo(LocalTime.of(14, 33))
            advanceTimeBy(1_000)
            assertThat(awaitItem().time).isEqualTo(LocalTime.of(14, 34))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun exposesDateAndDayFraction() = runTest {
        val provider = FakeTimeProvider(
            LocalDateTime.of(2026, 6, 16, 6, 0, 0),
            stepSeconds = 0
        )
        val clock = FrameClock(provider, StandardTestDispatcher(testScheduler))
        clock.now.test {
            val tick = awaitItem()
            assertThat(tick.date).isEqualTo(LocalDate.of(2026, 6, 16))
            // 06:00 = quarter of the day
            assertThat(tick.dayFraction).isWithin(1e-4f).of(0.25f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
