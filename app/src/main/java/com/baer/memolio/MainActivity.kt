package com.baer.memolio

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.baer.memolio.appliance.KioskController
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.server.UploadEventBus
import com.baer.memolio.core.ui.MemolioTheme
import com.baer.memolio.core.ui.UploadFeedbackOverlay
import com.baer.memolio.core.ui.rememberEntitlement
import com.baer.memolio.feature.frame.FrameRoute
import com.baer.memolio.feature.manage.ManageScaffold
import com.baer.memolio.feature.onboard.OnboardScreen
import com.baer.memolio.feature.paywall.PaywallScreen
import com.baer.memolio.navigation.MemolioNavHost
import com.baer.memolio.navigation.startDestination
import com.baer.memolio.service.FrameService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var entitlementRepository: EntitlementRepository
    @Inject lateinit var uploadEventBus: UploadEventBus

    private var frameService: FrameService? = null
    private val sleeping = MutableStateFlow(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val svc = (binder as? FrameService.LocalBinder)?.service ?: return
            frameService = svc
            // Mirror service display state into the Compose tree / window.
            lifecycleScope.launch { svc.sleeping.collect { sleeping.value = it } }
            lifecycleScope.launch { svc.targetBrightness.collect { applyBrightness(it) } }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            frameService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen from the first frame: it's a photo frame, system bars are never wanted.
        enterImmersive()

        // Apply the kiosk plan (lock-task / immersive / keep-screen-on) whenever it changes.
        lifecycleScope.launch {
            combine(settingsRepository.appSettings, entitlementRepository.isPro) { s, pro -> s.kioskEnabled to pro }
                .collect { (kioskEnabled, pro) ->
                    KioskController.apply(this@MainActivity, KioskController.plan(kioskEnabled, pro))
                }
        }

        setContent {
            MemolioTheme {
                // Android 13+ needs an explicit grant or the foreground-service
                // notification that keeps the frame alive is silently suppressed.
                RequestNotificationPermission()
                // null until the persisted flag loads: render an empty Surface rather
                // than flashing Onboard before the real onboarding state arrives.
                val settings by settingsRepository.appSettings.collectAsState(initial = null)
                Surface(modifier = Modifier.fillMaxSize()) {
                    val start = startDestination(settings?.onboardingComplete)
                    if (start != null) {
                        ApplianceHost(sleeping = sleeping, onWake = ::wake) {
                            UploadFeedbackOverlay(events = uploadEventBus.events) {
                                MemolioNavHost(
                                    start = start,
                                    frameContent = { onOpenManage -> FrameRoute(onOpenManage = onOpenManage) },
                                    manageContent = { onOpenPaywall, onClose ->
                                        ManageScaffold(
                                            isPro = rememberEntitlement(entitlementRepository),
                                            onOpenPaywall = onOpenPaywall,
                                            onClose = onClose
                                        )
                                    },
                                    onboardContent = { onFinished, onOpenPaywall ->
                                        OnboardScreen(onFinished = onFinished, onOpenPaywall = onOpenPaywall)
                                    },
                                    paywallContent = { onClose -> PaywallScreen(onClose = onClose) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.startForegroundService(this, Intent(this, FrameService::class.java))
        bindService(Intent(this, FrameService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        runCatching { unbindService(connection) }
    }

    /** Re-assert fullscreen when the window regains focus (OEMs re-show bars on return). */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersive()
    }

    /** Edge-to-edge + hide the status/nav bars (swipe reveals them transiently). */
    private fun enterImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /** Apply the ambient/manual target brightness to this window. */
    private fun applyBrightness(value: Float) {
        val lp = window.attributes
        lp.screenBrightness = value.coerceIn(0.01f, 1f)
        window.attributes = lp
    }

    /** Tap-to-wake passthrough to the service's SleepDriver. */
    private fun wake() {
        frameService?.wake()
    }
}

/**
 * Asks for POST_NOTIFICATIONS once per composition on Android 13+ when not already
 * granted. No-op below 33 (the permission did not exist). The result is ignored: the
 * frame works either way, but a granted notification is required for the FGS to show.
 */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result ignored — frame runs regardless */ }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun ApplianceHost(
    sleeping: StateFlow<Boolean>,
    onWake: () -> Unit,
    content: @Composable () -> Unit
) {
    val isSleeping by sleeping.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (isSleeping) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onWake() }
            )
        }
    }
}
