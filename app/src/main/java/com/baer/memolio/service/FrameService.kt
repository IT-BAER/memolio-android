package com.baer.memolio.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.server.FrameServer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that hosts the embedded [FrameServer] so uploads land even while the
 * frame is dimmed or asleep. Phase 2 scope = start/stop the server + post the foreground
 * notification. Phase 5 EXTENDS this class to also own a SleepScheduler + AmbientDimmer and
 * to be (re)started by a BootReceiver.
 *
 * Seams left open for Phase 5:
 * - [Companion.start]/[Companion.stop] are the canonical entry points (BootReceiver and the
 *   onboarding flow call these; nothing should `startService` directly).
 * - Server bring-up is isolated in [startServer] so Phase 5 can add scheduler/dimmer wiring
 *   alongside it without reworking [onStartCommand].
 * - The app-scoped [scope] (SupervisorJob, Default dispatcher) is reused for any future
 *   collectors (sleep window, ambient light) and is cancelled in [onDestroy].
 *
 * Android-14+ FGS-type note (spec §10): an always-on local upload server has no first-class
 * foreground-service type. The primary deployment runs inside the genuinely-foreground frame
 * activity (no FGS time limit). This Phase-2 skeleton calls the untyped [startForeground]
 * overload, which is valid on minSdk 26 and pairs with whatever `foregroundServiceType` the
 * Task 11 / Phase 5 manifest declares (`specialUse`). The typed `startForeground(id, n, type)`
 * variant + the matching manifest `<service>`/`<property>`/BootReceiver are deferred to
 * Task 11 / Phase 5 on purpose; on API < 34 the type attribute is ignored anyway.
 */
@AndroidEntryPoint
class FrameService : Service() {

    @Inject lateinit var frameServer: FrameServer
    @Inject lateinit var settings: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        FrameNotification.ensureChannel(this)
        startForeground(
            FrameNotification.NOTIFICATION_ID,
            FrameNotification.build(this, "Ready to receive photos")
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServer()
        // START_STICKY: the OS recreates the always-on frame server after it kills the
        // process under memory pressure (no command to redeliver — bring-up is idempotent).
        return START_STICKY
    }

    /**
     * Brings the upload server up. Generates the token on first run (so the pairing QR is
     * valid immediately) and reads the configured port, then starts [FrameServer]. Phase 5
     * adds sleep/dimming bring-up here. [FrameServer.start] is a no-op if already running,
     * so repeated `onStartCommand`s are safe.
     */
    private fun startServer() {
        scope.launch {
            settings.ensureToken()
            val port = settings.appSettings.first().serverPort
            frameServer.start(port)
        }
    }

    override fun onDestroy() {
        frameServer.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private fun intent(context: Context) = Intent(context, FrameService::class.java)

        /** Starts the always-on frame server as a foreground service. */
        fun start(context: Context) {
            context.startForegroundService(intent(context))
        }

        /** Stops the service (and, via [onDestroy], the embedded server). */
        fun stop(context: Context) {
            context.stopService(intent(context))
        }
    }
}
