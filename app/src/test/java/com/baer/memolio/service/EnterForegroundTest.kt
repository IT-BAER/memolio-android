package com.baer.memolio.service

import android.app.ForegroundServiceStartNotAllowedException
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EnterForegroundTest {

    @Test
    fun deniedStartReportsFalseAndCallsOnDenied() {
        var denied = 0
        val entered = enterForeground(
            start = { throw ForegroundServiceStartNotAllowedException("mAllowStartForeground false") },
            onDenied = { denied++ }
        )
        assertThat(entered).isFalse()
        assertThat(denied).isEqualTo(1)
    }

    @Test
    fun allowedStartReportsTrueWithoutOnDenied() {
        var denied = 0
        val entered = enterForeground(start = {}, onDenied = { denied++ })
        assertThat(entered).isTrue()
        assertThat(denied).isEqualTo(0)
    }

    @Test
    fun otherIllegalStateIsNotSwallowed() {
        var denied = 0
        assertThrows(IllegalStateException::class.java) {
            enterForeground(start = { throw IllegalStateException("other") }, onDenied = { denied++ })
        }
        assertThat(denied).isEqualTo(0)
    }
}
