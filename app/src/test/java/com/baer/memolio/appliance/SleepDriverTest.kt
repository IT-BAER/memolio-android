package com.baer.memolio.appliance

import app.cash.turbine.test
import com.baer.memolio.core.datastore.AppSettings
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SleepDriverTest {

    private fun settings(
        enabled: Boolean = true,
        start: Int = 22 * 60,
        end: Int = 7 * 60
    ) = AppSettings(sleepEnabled = enabled, sleepStartMinutes = start, sleepEndMinutes = end)

    @Test
    fun emitsAsleepWhenTickFallsInsideWindow() = runTest {
        val ticks = MutableSharedFlow<Unit>(replay = 1)
        val settingsFlow = MutableStateFlow(settings())
        val minutes = MutableStateFlow(23 * 60)
        val driver = SleepDriver(
            ticks = ticks,
            appSettings = settingsFlow,
            time = TimeProvider { minutes.value },
            scope = backgroundScope,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )
        ticks.emit(Unit)
        driver.sleeping.test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun emitsAwakeWhenTickFallsOutsideWindow() = runTest {
        val ticks = MutableSharedFlow<Unit>(replay = 1)
        val settingsFlow = MutableStateFlow(settings())
        val minutes = MutableStateFlow(12 * 60)
        val driver = SleepDriver(
            ticks = ticks,
            appSettings = settingsFlow,
            time = TimeProvider { minutes.value },
            scope = backgroundScope,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )
        ticks.emit(Unit)
        driver.sleeping.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun disablingSleepWakesEvenInsideWindow() = runTest {
        val ticks = MutableSharedFlow<Unit>(replay = 1)
        val settingsFlow = MutableStateFlow(settings(enabled = true))
        val minutes = MutableStateFlow(23 * 60)
        val driver = SleepDriver(
            ticks = ticks,
            appSettings = settingsFlow,
            time = TimeProvider { minutes.value },
            scope = backgroundScope,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )
        ticks.emit(Unit)
        driver.sleeping.test {
            assertThat(awaitItem()).isTrue()
            settingsFlow.value = settings(enabled = false)
            ticks.emit(Unit)
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun wakeOverridesScheduleUntilNextTick() = runTest {
        val ticks = MutableSharedFlow<Unit>(replay = 1)
        val settingsFlow = MutableStateFlow(settings())
        val minutes = MutableStateFlow(23 * 60)
        val driver = SleepDriver(
            ticks = ticks,
            appSettings = settingsFlow,
            time = TimeProvider { minutes.value },
            scope = backgroundScope,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )
        ticks.emit(Unit)
        driver.sleeping.test {
            assertThat(awaitItem()).isTrue()
            driver.wake()
            assertThat(awaitItem()).isFalse()
        }
    }
}
