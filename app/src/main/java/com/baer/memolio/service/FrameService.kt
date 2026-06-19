package com.baer.memolio.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.baer.memolio.appliance.AmbientDimmer
import com.baer.memolio.appliance.SleepDriver
import com.baer.memolio.appliance.TimeProvider
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.server.FrameServer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that hosts the embedded [FrameServer] so uploads land even while the
 * frame is dimmed or asleep. Owns the appliance display drivers: a [SleepDriver] (scheduled
 * sleep) and an [AmbientDimmer] (light-sensor brightness). The activity binds to read the
 * resulting [sleeping] / [targetBrightness] state and applies it to its window.
 *
 * The server stays up 24/7, including during scheduled sleep — only the DISPLAY dims/blacks
 * (spec section 5). Nothing here stops [FrameServer] when sleeping.
 */
@AndroidEntryPoint
class FrameService : Service() {

    @Inject lateinit var frameServer: FrameServer
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var entitlementRepository: EntitlementRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var ambientDimmer: AmbientDimmer
    private lateinit var sleepDriver: SleepDriver

    /** Display state exposed to a bound activity. */
    val sleeping: StateFlow<Boolean> get() = sleepDriver.sleeping
    val targetBrightness: StateFlow<Float> get() = ambientDimmer.targetBrightness

    /** 60s tick so the schedule + dim re-evaluate without busy-waiting. */
    private val minuteTicks: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(60_000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        FrameNotification.ensureChannel(this)
        startForegroundCompat()

        ambientDimmer = AmbientDimmer(applicationContext)

        val gatedSettings = combine(settings.appSettings, entitlementRepository.isPro) { s, pro ->
            if (pro) s else s.copy(sleepEnabled = false)
        }
        sleepDriver = SleepDriver(
            ticks = minuteTicks,
            appSettings = gatedSettings,
            time = timeProvider,
            scope = scope,
            dispatcher = Dispatchers.Default
        )
        ambientDimmer.start()

        // Push brightness/dimming settings into the dimmer only when Pro.
        combine(settings.appSettings, entitlementRepository.isPro) { s, pro -> s to pro }
            .onEach { (s, pro) ->
                ambientDimmer.configure(dimmingEnabled = s.ambientDimming && pro, manualBrightness = s.brightness)
            }
            .launchIn(scope)
    }

    private fun startForegroundCompat() {
        val id = FrameNotification.NOTIFICATION_ID
        val notification = FrameNotification.build(this, "Ready to receive photos")
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                startForeground(id, notification, 0)
            else ->
                startForeground(id, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServer()
        // START_STICKY: the OS recreates the always-on frame server after it kills the
        // process under memory pressure (no command to redeliver — bring-up is idempotent).
        return START_STICKY
    }

    /**
     * Brings the upload server up. Generates the token on first run (so the pairing QR is
     * valid immediately) and reads the configured port, then starts [FrameServer].
     * [FrameServer.start] is a no-op if already running, so repeated `onStartCommand`s
     * are safe. The server stays up 24/7, including during scheduled sleep.
     */
    private fun startServer() {
        scope.launch {
            settings.ensureToken()
            val port = settings.appSettings.first().serverPort
            frameServer.start(port)
        }
    }

    /** Tap-to-wake: force the display awake; the schedule resumes on the next minute tick. */
    fun wake() {
        sleepDriver.wake()
    }

    override fun onDestroy() {
        ambientDimmer.stop()
        frameServer.stop()
        scope.cancel()
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        val service: FrameService get() = this@FrameService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

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
