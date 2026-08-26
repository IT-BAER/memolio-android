package com.baer.memolio.core.server

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UploadUrlProviderTest {

    @Test
    fun combinesAddressAndTokenIntoUrl() = runTest {
        val address = MutableStateFlow<String?>("192.168.1.42:8080")
        val provider = UploadUrlProviderImpl(address, flowOf("tok123"), flowOf(true))
        provider.uploadUrl.test {
            assertThat(awaitItem()).isEqualTo("http://192.168.1.42:8080/?t=tok123")
        }
    }

    @Test
    fun nullAddressYieldsNullUrl() = runTest {
        val address = MutableStateFlow<String?>(null)
        val provider = UploadUrlProviderImpl(address, flowOf("tok123"), flowOf(true))
        provider.uploadUrl.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun emptyTokenYieldsNullUrl() = runTest {
        val address = MutableStateFlow<String?>("10.0.0.5:8080")
        val provider = UploadUrlProviderImpl(address, flowOf(""), flowOf(true))
        provider.uploadUrl.test {
            assertThat(awaitItem()).isNull()
        }
    }

    /**
     * The pairing UI needs the server-bound flag separately from the URL: no URL plus a
     * bound server means "no Wi-Fi", no URL plus an unbound server means "still starting".
     */
    @Test
    fun serverRunningIsExposedSeparatelyFromTheUrl() = runTest {
        val address = MutableStateFlow<String?>(null)
        val running = MutableStateFlow(false)
        val provider = UploadUrlProviderImpl(address, flowOf("tok123"), running)
        provider.serverRunning.test {
            assertThat(awaitItem()).isFalse()
            running.value = true
            assertThat(awaitItem()).isTrue()
        }
    }
}
