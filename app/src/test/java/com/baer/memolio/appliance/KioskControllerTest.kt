package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KioskControllerTest {

    @Test
    fun kioskDisabledNoLockTaskButFrameStillImmersive() {
        val plan = KioskController.plan(kioskEnabled = false, isPro = true, onFrame = true)
        assertThat(plan.lockTask).isFalse()
        assertThat(plan.immersive).isTrue()
    }

    @Test
    fun kioskEnabledLocksAndGoesImmersive() {
        val plan = KioskController.plan(kioskEnabled = true, isPro = true, onFrame = true)
        assertThat(plan.lockTask).isTrue()
        assertThat(plan.immersive).isTrue()
    }

    @Test
    fun frameIsAlwaysImmersiveEvenForFreeUsers() {
        assertThat(KioskController.plan(kioskEnabled = false, isPro = false, onFrame = true).immersive).isTrue()
        assertThat(KioskController.plan(kioskEnabled = true, isPro = false, onFrame = true).immersive).isTrue()
    }

    @Test
    fun manageShowsSystemBarsSoGestureNavWorks() {
        // Off the frame (Manage/Onboard/Paywall) the bars come back unless kiosk locks the app.
        assertThat(KioskController.plan(kioskEnabled = false, isPro = true, onFrame = false).immersive).isFalse()
        assertThat(KioskController.plan(kioskEnabled = false, isPro = false, onFrame = false).immersive).isFalse()
    }

    @Test
    fun kioskKeepsImmersiveEverywhereWhenPro() {
        assertThat(KioskController.plan(kioskEnabled = true, isPro = true, onFrame = false).immersive).isTrue()
        // Kiosk without Pro is inert, so bars return off-frame.
        assertThat(KioskController.plan(kioskEnabled = true, isPro = false, onFrame = false).immersive).isFalse()
    }

    @Test
    fun keepScreenOnWhileFramingRegardlessOfKiosk() {
        assertThat(KioskController.plan(kioskEnabled = false, isPro = true, onFrame = true).keepScreenOn).isTrue()
        assertThat(KioskController.plan(kioskEnabled = true, isPro = true, onFrame = true).keepScreenOn).isTrue()
    }

    @Test
    fun planChangeIsDetectable() {
        val off = KioskController.plan(false, isPro = true, onFrame = true)
        val on = KioskController.plan(true, isPro = true, onFrame = true)
        assertThat(off).isNotEqualTo(on)
    }
}
