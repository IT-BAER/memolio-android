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
}
