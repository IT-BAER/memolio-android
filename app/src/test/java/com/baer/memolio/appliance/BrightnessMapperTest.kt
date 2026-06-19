package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BrightnessMapperTest {

    @Test
    fun darkRoomMapsToFloor() {
        assertThat(BrightnessMapper.luxToBrightness(0f)).isWithin(0.001f).of(0.05f)
    }

    @Test
    fun negativeLuxClampsToFloor() {
        assertThat(BrightnessMapper.luxToBrightness(-50f)).isWithin(0.001f).of(0.05f)
    }

    @Test
    fun brightRoomMapsToCeiling() {
        assertThat(BrightnessMapper.luxToBrightness(2000f)).isWithin(0.001f).of(1.0f)
    }

    @Test
    fun veryBrightClampsToCeiling() {
        assertThat(BrightnessMapper.luxToBrightness(50_000f)).isWithin(0.001f).of(1.0f)
    }

    @Test
    fun midRangeIsBetweenFloorAndCeiling() {
        val b = BrightnessMapper.luxToBrightness(200f)
        assertThat(b).isGreaterThan(0.05f)
        assertThat(b).isLessThan(1.0f)
    }

    @Test
    fun monotonicNonDecreasing() {
        var prev = -1f
        var lux = 0f
        while (lux <= 2000f) {
            val b = BrightnessMapper.luxToBrightness(lux)
            assertThat(b).isAtLeast(prev)
            prev = b
            lux += 25f
        }
    }

    @Test
    fun customRangeRespected() {
        assertThat(BrightnessMapper.luxToBrightness(100f, floor = 0.2f, maxLux = 100f))
            .isWithin(0.001f).of(1.0f)
        assertThat(BrightnessMapper.luxToBrightness(0f, floor = 0.2f, maxLux = 100f))
            .isWithin(0.001f).of(0.2f)
    }
}
