package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KioskControllerTest {

    @Test
    fun kioskDisabledNoLockTaskNoImmersive() {
        val plan = KioskController.plan(kioskEnabled = false, isPro = true)
        assertThat(plan.lockTask).isFalse()
        assertThat(plan.immersive).isFalse()
    }

    @Test
    fun kioskEnabledLocksAndGoesImmersive() {
        val plan = KioskController.plan(kioskEnabled = true, isPro = true)
        assertThat(plan.lockTask).isTrue()
        assertThat(plan.immersive).isTrue()
    }

    @Test
    fun keepScreenOnWhileFramingRegardlessOfKiosk() {
        assertThat(KioskController.plan(kioskEnabled = false, isPro = true).keepScreenOn).isTrue()
        assertThat(KioskController.plan(kioskEnabled = true, isPro = true).keepScreenOn).isTrue()
    }

    @Test
    fun planChangeIsDetectable() {
        val off = KioskController.plan(false, isPro = true)
        val on = KioskController.plan(true, isPro = true)
        assertThat(off).isNotEqualTo(on)
    }
}
