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
        val provider = UploadUrlProviderImpl(address, flowOf("tok123"))
        provider.uploadUrl.test {
            assertThat(awaitItem()).isEqualTo("http://192.168.1.42:8080/?t=tok123")
        }
    }

    @Test
    fun nullAddressYieldsNullUrl() = runTest {
        val address = MutableStateFlow<String?>(null)
        val provider = UploadUrlProviderImpl(address, flowOf("tok123"))
        provider.uploadUrl.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun emptyTokenYieldsNullUrl() = runTest {
        val address = MutableStateFlow<String?>("10.0.0.5:8080")
        val provider = UploadUrlProviderImpl(address, flowOf(""))
        provider.uploadUrl.test {
            assertThat(awaitItem()).isNull()
        }
    }
}
