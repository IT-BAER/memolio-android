package com.baer.memolio.appliance

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BootReceiverTest {

    private class FakeLauncher : ApplianceLauncher {
        var launched = 0
        override fun launchFrame() { launched++ }
    }

    @Test
    fun launchesWhenBootCompletedAndAutostartEnabled() = runTest {
        val launcher = FakeLauncher()
        BootReceiver.handle(Intent.ACTION_BOOT_COMPLETED, autostartEnabled = true, isPro = true, launcher = launcher)
        assertThat(launcher.launched).isEqualTo(1)
    }

    @Test
    fun doesNotLaunchWhenAutostartDisabled() = runTest {
        val launcher = FakeLauncher()
        BootReceiver.handle(Intent.ACTION_BOOT_COMPLETED, autostartEnabled = false, isPro = true, launcher = launcher)
        assertThat(launcher.launched).isEqualTo(0)
    }

    @Test
    fun ignoresUnrelatedActions() = runTest {
        val launcher = FakeLauncher()
        BootReceiver.handle(Intent.ACTION_BATTERY_LOW, autostartEnabled = true, isPro = true, launcher = launcher)
        assertThat(launcher.launched).isEqualTo(0)
    }

    @Test
    fun acceptsQuickbootPowerOn() = runTest {
        val launcher = FakeLauncher()
        BootReceiver.handle("android.intent.action.QUICKBOOT_POWERON", autostartEnabled = true, isPro = true, launcher = launcher)
        assertThat(launcher.launched).isEqualTo(1)
    }
}
