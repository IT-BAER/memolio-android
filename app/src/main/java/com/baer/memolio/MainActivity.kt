package com.baer.memolio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.baer.memolio.appliance.KioskController
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.ui.MemolioTheme
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var entitlementRepository: EntitlementRepository

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

        // Apply the kiosk plan (lock-task / immersive / keep-screen-on) whenever it changes.
        lifecycleScope.launch {
            combine(settingsRepository.appSettings, entitlementRepository.isPro) { s, pro -> s.kioskEnabled to pro }
                .collect { (kioskEnabled, pro) ->
                    KioskController.apply(this@MainActivity, KioskController.plan(kioskEnabled, pro))
                }
        }

        setContent {
            MemolioTheme {
                val settings by settingsRepository.appSettings.collectAsState(initial = AppSettings())
                Surface(modifier = Modifier.fillMaxSize()) {
                    ApplianceHost(sleeping = sleeping.asStateFlow(), onWake = ::wake) {
                        MemolioNavHost(
                            start = startDestination(settings.onboardingComplete),
                            frameContent = { onOpenManage -> FrameRoute(onOpenManage = onOpenManage) },
                            manageContent = { onOpenPaywall ->
                                ManageScaffold(
                                    isPro = rememberEntitlement(entitlementRepository),
                                    onOpenPaywall = onOpenPaywall
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

    override fun onStart() {
        super.onStart()
        ContextCompat.startForegroundService(this, Intent(this, FrameService::class.java))
        bindService(Intent(this, FrameService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        runCatching { unbindService(connection) }
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
