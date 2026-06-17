package com.baer.memolio.core.server

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsTokenProviderTest {

    @Test
    fun startsEmptyThenReflectsCollectedToken() = runTest {
        val tokens = MutableStateFlow("")
        val provider = SettingsTokenProvider.startFrom(backgroundScope, tokens)
        // Collection runs on the test scheduler; advance it.
        runCurrent()
        assertThat(provider.current()).isEqualTo("")

        tokens.value = "first-token"
        runCurrent()
        assertThat(provider.current()).isEqualTo("first-token")
    }

    @Test
    fun rotatedTokenIsReflectedImmediately() = runTest {
        // Addendum C: rotateToken() (modeled here as a new flow emission) must invalidate
        // the old token on the very next current() read, without any restart.
        val tokens = MutableStateFlow("old")
        val provider = SettingsTokenProvider.startFrom(backgroundScope, tokens)
        runCurrent()
        assertThat(provider.current()).isEqualTo("old")

        tokens.value = "rotated"
        runCurrent()
        assertThat(provider.current()).isEqualTo("rotated")
        assertThat(provider.current()).isNotEqualTo("old")
    }
}
