package com.baer.memolio.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StartDestinationTest {
    @Test
    fun onboardingIncompleteStartsAtOnboard() {
        assertThat(startDestination(onboardingComplete = false)).isEqualTo(StartDestination.Onboard)
    }

    @Test
    fun onboardingCompleteStartsAtFrame() {
        assertThat(startDestination(onboardingComplete = true)).isEqualTo(StartDestination.Frame)
    }
}
