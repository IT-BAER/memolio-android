package com.baer.memolio.appliance

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.baer.memolio.MainActivity
import com.baer.memolio.service.FrameService

/**
 * Seam for "bring the frame to life": start the foreground [FrameService] and launch
 * [MainActivity]. Faked in BootReceiver unit tests so the gate logic is testable
 * without actually starting components.
 */
fun interface ApplianceLauncher {
    fun launchFrame()

    companion object {
        fun real(context: Context): ApplianceLauncher = ApplianceLauncher {
            val app = context.applicationContext
            ContextCompat.startForegroundService(app, Intent(app, FrameService::class.java))
            app.startActivity(
                Intent(app, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
