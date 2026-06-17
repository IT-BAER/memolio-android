package com.baer.memolio.service

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FrameNotificationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun ensureChannelCreatesFrameChannel() {
        FrameNotification.ensureChannel(context)
        val nm = context.getSystemService(NotificationManager::class.java)
        assertThat(nm.getNotificationChannel(FrameNotification.CHANNEL_ID)).isNotNull()
    }

    @Test
    fun buildReturnsOngoingNotification() {
        FrameNotification.ensureChannel(context)
        val notification = FrameNotification.build(context, "Frame running")
        assertThat(notification).isNotNull()
        assertThat(FrameNotification.NOTIFICATION_ID).isGreaterThan(0)
    }
}
