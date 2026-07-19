package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AmbientDimmerTest {

    @Test
    fun usesManualBrightnessWhenDimmingOff() {
        val b = AmbientDimmer.resolveBrightness(
            dimmingEnabled = false, hasSensor = true, lastLux = 1500f, manualBrightness = 0.42f
        )
        assertThat(b).isWithin(0.001f).of(0.42f)
    }

    @Test
    fun usesManualBrightnessWhenNoSensor() {
        val b = AmbientDimmer.resolveBrightness(
            dimmingEnabled = true, hasSensor = false, lastLux = 1500f, manualBrightness = 0.6f
        )
        assertThat(b).isWithin(0.001f).of(0.6f)
    }

    @Test
    fun usesManualBrightnessWhenNoLuxReadingYet() {
        val b = AmbientDimmer.resolveBrightness(
            dimmingEnabled = true, hasSensor = true, lastLux = null, manualBrightness = 0.6f
        )
        assertThat(b).isWithin(0.001f).of(0.6f)
    }

    @Test
    fun mapsLuxWhenDimmingOnSensorPresentAndReadingAvailable() {
        val b = AmbientDimmer.resolveBrightness(
            dimmingEnabled = true, hasSensor = true, lastLux = 5_000f, manualBrightness = 0.1f
        )
        assertThat(b).isWithin(0.001f).of(BrightnessMapper.luxToBrightness(5_000f))
    }

    // Smoothing: sensor jitter must not flicker the screen.

    @Test
    fun smallJitterInsideDeadbandLeavesBrightnessUnchanged() {
        assertThat(AmbientDimmer.smoothed(previous = 0.50f, target = 0.52f)).isWithin(0.0001f).of(0.50f)
        assertThat(AmbientDimmer.smoothed(previous = 0.50f, target = 0.48f)).isWithin(0.0001f).of(0.50f)
    }

    @Test
    fun largeChangeMovesGraduallyNotInstantly() {
        val next = AmbientDimmer.smoothed(previous = 0.10f, target = 0.90f)
        assertThat(next).isGreaterThan(0.10f)
        assertThat(next).isLessThan(0.50f) // eased, not jumped
    }

    @Test
    fun repeatedStepsConvergeNearTarget() {
        var b = 0.10f
        repeat(50) { b = AmbientDimmer.smoothed(previous = b, target = 0.90f) }
        assertThat(b).isWithin(0.06f).of(0.90f) // within deadband of target, then stable
        val settled = AmbientDimmer.smoothed(previous = b, target = 0.90f)
        assertThat(settled).isWithin(0.0001f).of(b)
    }
}
