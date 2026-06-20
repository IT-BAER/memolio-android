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

    @Test
    fun unknownOnboardingStateIsUndecided() {
        // Until the persisted flag loads we must NOT default to Onboard, or the
        // welcome screen flashes on every cold start before jumping to the frame.
        assertThat(startDestination(onboardingComplete = null)).isNull()
    }
}
