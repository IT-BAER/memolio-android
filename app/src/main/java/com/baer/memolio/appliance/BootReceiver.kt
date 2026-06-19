package com.baer.memolio.appliance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.datastore.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Relaunches the frame on power-on, gated on AppSettings.autostartEnabled (spec section 5).
 * Pure dispatch is in [handle] (unit-tested with a fake launcher). onReceive reads the
 * setting and delegates. Registered for RECEIVE_BOOT_COMPLETED + QUICKBOOT_POWERON.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var entitlementRepository: EntitlementRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != QUICKBOOT_POWERON) return
        val autostart = runBlocking { settingsRepository.appSettings.first().autostartEnabled }
        val pro = runBlocking { entitlementRepository.isPro.first() }
        handle(action, autostart, pro, ApplianceLauncher.real(context))
    }

    companion object {
        const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"

        /** Pure dispatch: launch only on a boot action when autostart is enabled and Pro. */
        fun handle(action: String, autostartEnabled: Boolean, isPro: Boolean, launcher: ApplianceLauncher) {
            val isBoot = action == Intent.ACTION_BOOT_COMPLETED || action == QUICKBOOT_POWERON
            if (isBoot && autostartEnabled && isPro) launcher.launchFrame()
        }
    }
}
