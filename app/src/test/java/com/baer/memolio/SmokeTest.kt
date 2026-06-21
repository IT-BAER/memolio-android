package com.baer.memolio

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmokeTest {
    @Test
    fun packageNameIsCorrect() {
        // Debug builds append a .debug applicationId suffix; release has none.
        assertThat(BuildConfig.APPLICATION_ID).startsWith("com.baer.memolio")
    }
}
