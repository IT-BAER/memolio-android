package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BrightnessEaserTest {

    // The light sensor is on-change: once lux stops changing it sends nothing more.
    @Test
    fun settlesAtTargetWithoutFurtherUpdates() = runTest {
        val easer = BrightnessEaser(backgroundScope, StandardTestDispatcher(testScheduler), initial = 0.52f)
        easer.easeTo(0.05f)
        advanceTimeBy(5_000)
        runCurrent()
        assertThat(easer.brightness.value).isWithin(0.05f).of(0.05f)
    }

    @Test
    fun firstStepEasesInsteadOfJumping() = runTest {
        val easer = BrightnessEaser(backgroundScope, StandardTestDispatcher(testScheduler), initial = 0.10f)
        easer.easeTo(0.90f)
        runCurrent()
        assertThat(easer.brightness.value).isGreaterThan(0.10f)
        assertThat(easer.brightness.value).isLessThan(0.50f)
    }

    @Test
    fun followsNewTargetWhileEasing() = runTest {
        val easer = BrightnessEaser(backgroundScope, StandardTestDispatcher(testScheduler), initial = 0.10f)
        easer.easeTo(0.90f)
        advanceTimeBy(400)
        easer.easeTo(0.05f)
        advanceTimeBy(5_000)
        runCurrent()
        assertThat(easer.brightness.value).isWithin(0.05f).of(0.05f)
    }

    @Test
    fun jumpToAppliesAtOnce() = runTest {
        val easer = BrightnessEaser(backgroundScope, StandardTestDispatcher(testScheduler), initial = 0.10f)
        easer.jumpTo(0.70f)
        assertThat(easer.brightness.value).isWithin(0.0001f).of(0.70f)
    }

    @Test
    fun easesAgainAfterSettling() = runTest {
        val easer = BrightnessEaser(backgroundScope, StandardTestDispatcher(testScheduler), initial = 0.52f)
        easer.easeTo(0.05f)
        advanceTimeBy(5_000)
        runCurrent()
        easer.easeTo(0.90f)
        advanceTimeBy(5_000)
        runCurrent()
        assertThat(easer.brightness.value).isWithin(0.05f).of(0.90f)
    }
}
