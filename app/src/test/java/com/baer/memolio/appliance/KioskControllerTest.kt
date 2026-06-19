package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KioskControllerTest {

    @Test
    fun kioskDisabledNoLockTaskNoImmersive() {
        val plan = KioskController.plan(kioskEnabled = false)
        assertThat(plan.lockTask).isFalse()
        assertThat(plan.immersive).isFalse()
    }

    @Test
    fun kioskEnabledLocksAndGoesImmersive() {
        val plan = KioskController.plan(kioskEnabled = true)
        assertThat(plan.lockTask).isTrue()
        assertThat(plan.immersive).isTrue()
    }

    @Test
    fun keepScreenOnWhileFramingRegardlessOfKiosk() {
        assertThat(KioskController.plan(kioskEnabled = false).keepScreenOn).isTrue()
        assertThat(KioskController.plan(kioskEnabled = true).keepScreenOn).isTrue()
    }

    @Test
    fun planChangeIsDetectable() {
        val off = KioskController.plan(false)
        val on = KioskController.plan(true)
        assertThat(off).isNotEqualTo(on)
    }
}
