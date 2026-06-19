package com.baer.memolio.appliance

import com.baer.memolio.core.datastore.AppSettings
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Proves the observation CONTRACT used by the live components:
 *  - KioskController.plan reacts to kioskEnabled
 *  - AmbientDimmer.resolveBrightness reacts to ambientDimming + brightness
 *  - SleepWindow.shouldSleep reacts to sleepEnabled/start/end
 * FrameService/MainActivity feed AppSettings fields straight into these (Task 9), so a
 * change to a setting changes the live behavior. This locks the field->behavior mapping.
 */
class SettingsObservationTest {

    @Test
    fun kioskToggleFlipsLockTaskPlan() {
        val off = AppSettings(kioskEnabled = false)
        val on = AppSettings(kioskEnabled = true)
        assertThat(KioskController.plan(off.kioskEnabled, isPro = true).lockTask).isFalse()
        assertThat(KioskController.plan(on.kioskEnabled, isPro = true).lockTask).isTrue()
    }

    @Test
    fun ambientToggleSwitchesBetweenSensorAndManual() {
        val manual = AmbientDimmer.resolveBrightness(
            dimmingEnabled = AppSettings(ambientDimming = false, brightness = 0.3f).ambientDimming,
            hasSensor = true, lastLux = 1000f,
            manualBrightness = 0.3f
        )
        assertThat(manual).isWithin(0.001f).of(0.3f)

        val auto = AmbientDimmer.resolveBrightness(
            dimmingEnabled = AppSettings(ambientDimming = true).ambientDimming,
            hasSensor = true, lastLux = 1000f, manualBrightness = 0.3f
        )
        assertThat(auto).isWithin(0.001f).of(BrightnessMapper.luxToBrightness(1000f))
    }

    @Test
    fun sleepScheduleFieldsDriveSleepDecision() {
        val s = AppSettings(sleepEnabled = true, sleepStartMinutes = 22 * 60, sleepEndMinutes = 7 * 60)
        assertThat(SleepWindow.shouldSleep(23 * 60, s.sleepEnabled, s.sleepStartMinutes, s.sleepEndMinutes)).isTrue()
        assertThat(SleepWindow.shouldSleep(12 * 60, s.sleepEnabled, s.sleepStartMinutes, s.sleepEndMinutes)).isFalse()
    }

    @Test
    fun autoCleanupDefaultIsOff() {
        assertThat(AppSettings().autoCleanup).isFalse()
    }

    @Test
    fun autostartDefaultIsOn() {
        assertThat(AppSettings().autostartEnabled).isTrue()
    }
}
