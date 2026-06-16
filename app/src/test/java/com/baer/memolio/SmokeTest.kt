package com.baer.memolio

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmokeTest {
    @Test
    fun packageNameIsCorrect() {
        assertThat(BuildConfig.APPLICATION_ID).isEqualTo("com.baer.memolio")
    }
}
