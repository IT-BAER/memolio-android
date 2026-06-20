package com.baer.memolio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.baer.memolio.R

/**
 * Builds the ongoing foreground-service notification on the "frame" channel.
 *
 * Kept as a small standalone helper (no Hilt, no Service coupling) so the channel
 * creation + builder can be unit-tested under Robolectric in isolation, while the
 * untestable [FrameService] lifecycle stays thin. Phase 5 may enrich the notification
 * (e.g. an action to open the frame or pause uploads) without touching the service glue.
 */
object FrameNotification {
    const val CHANNEL_ID = "frame"
    const val NOTIFICATION_ID = 1001

    /** Idempotent: creates the low-importance "frame" channel once. */
    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = context.getString(R.string.notif_channel_desc)
                }
            )
        }
    }

    /** Builds the ongoing notification. Title "Memolio frame", body = [text]. */
    fun build(context: Context, text: String): Notification =
        Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setOngoing(true)
            .build()
}
