package com.baer.memolio.appliance

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApplianceGatingTest {

    private class FakeLauncher : ApplianceLauncher {
        var launched = 0
        override fun launchFrame() { launched++ }
    }

    @Test
    fun kioskLockTaskIsNoOpWhenNotPro() {
        val plan = KioskController.plan(kioskEnabled = true, isPro = false, onFrame = true)
        assertThat(plan.lockTask).isFalse()
        // The frame display itself is fullscreen; only the lock-task PIN is gated.
        assertThat(plan.immersive).isTrue()
        assertThat(plan.keepScreenOn).isTrue()
    }

    @Test
    fun kioskPlanLocksWhenProAndEnabled() {
        val plan = KioskController.plan(kioskEnabled = true, isPro = true, onFrame = true)
        assertThat(plan.lockTask).isTrue()
        assertThat(plan.immersive).isTrue()
    }

    @Test
    fun bootHandleDoesNotLaunchWhenNotPro() {
        val launcher = FakeLauncher()
        BootReceiver.handle(Intent.ACTION_BOOT_COMPLETED, autostartEnabled = true, isPro = false, launcher = launcher)
        assertThat(launcher.launched).isEqualTo(0)
    }

    @Test
    fun bootHandleLaunchesWhenProAndAutostart() {
        val launcher = FakeLauncher()
        BootReceiver.handle(Intent.ACTION_BOOT_COMPLETED, autostartEnabled = true, isPro = true, launcher = launcher)
        assertThat(launcher.launched).isEqualTo(1)
    }

    @Test
    fun sleepIsSuppressedWhenNotPro() {
        assertThat(SleepWindow.shouldSleep(23 * 60, enabled = true && false, 22 * 60, 7 * 60)).isFalse()
        assertThat(SleepWindow.shouldSleep(23 * 60, enabled = true && true, 22 * 60, 7 * 60)).isTrue()
    }
}
