package com.baer.memolio.feature.onboard

// Pure-helper tests for onboardCta(step). UI-level checks (back absent on first
// step, scrollable step body) live in OnboardCompletionGateTest via createComposeRule.
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OnboardScreenTest {
    @Test
    fun `welcome step shows next but not back`() {
        val cta = onboardCta(OnboardStep.Welcome)
        assertThat(cta.showBack).isFalse()
        assertThat(cta.isFinish).isFalse()
    }

    @Test
    fun `finish step shows finish button`() {
        val cta = onboardCta(OnboardStep.Finish)
        assertThat(cta.isFinish).isTrue()
        assertThat(cta.showBack).isTrue()
    }

    @Test
    fun `appliance step shows back and next`() {
        val cta = onboardCta(OnboardStep.Appliance)
        assertThat(cta.showBack).isTrue()
        assertThat(cta.isFinish).isFalse()
    }
}
