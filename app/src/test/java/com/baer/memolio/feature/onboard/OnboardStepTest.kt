package com.baer.memolio.feature.onboard

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OnboardStepTest {
    @Test
    fun `five steps in order`() {
        assertThat(OnboardStep.entries).containsExactly(
            OnboardStep.Welcome, OnboardStep.ShowQr, OnboardStep.Appliance,
            OnboardStep.GoPro, OnboardStep.Finish
        ).inOrder()
    }

    @Test
    fun `first and last flags`() {
        assertThat(OnboardStep.Welcome.isFirst).isTrue()
        assertThat(OnboardStep.Finish.isLast).isTrue()
        assertThat(OnboardStep.Welcome.isLast).isFalse()
    }

    @Test
    fun `next and previous clamp at ends`() {
        assertThat(OnboardStep.Welcome.previousOrSelf()).isEqualTo(OnboardStep.Welcome)
        assertThat(OnboardStep.Finish.nextOrSelf()).isEqualTo(OnboardStep.Finish)
        assertThat(OnboardStep.Welcome.nextOrSelf()).isEqualTo(OnboardStep.ShowQr)
    }
}
