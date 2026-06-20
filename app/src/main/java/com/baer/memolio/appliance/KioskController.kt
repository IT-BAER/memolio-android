package com.baer.memolio.appliance

import android.app.Activity
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Kiosk behavior. [plan] is PURE (unit-tested): it decides whether the frame should
 * lock-task, go immersive, and keep the screen on. [apply] performs the device-only
 * calls (lock task, hide system bars, FLAG_KEEP_SCREEN_ON) and requires a live
 * Activity/Window, so it is exercised only on-device (no JVM unit test).
 *
 * Note: startLockTask() pins the app. With no Device Owner it shows the OS "screen
 * pinned" confirmation; OEM skins differ (spec section 10 flagged risk). It silently
 * no-ops if the task cannot be locked.
 */
object KioskController {

    data class Plan(
        val lockTask: Boolean,
        val immersive: Boolean,
        val keepScreenOn: Boolean
    )

    /**
     * Pure decision. The frame is fullscreen ALWAYS (it's a photo frame — system bars
     * are never wanted), so [immersive] and [keepScreenOn] are unconditional. Only the
     * lock-task PIN (which traps the user in the app) is Pro-gated via APPLIANCE.
     */
    fun plan(kioskEnabled: Boolean, isPro: Boolean): Plan = Plan(
        lockTask = kioskEnabled && isPro,
        immersive = true,
        keepScreenOn = true
    )

    /** Device-only application of a [Plan] to a live [activity]. */
    fun apply(activity: Activity, plan: Plan) {
        val window = activity.window
        if (plan.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        WindowCompat.setDecorFitsSystemWindows(window, !plan.immersive)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (plan.immersive) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }

        if (plan.lockTask) {
            runCatching { activity.startLockTask() }
        } else {
            runCatching { activity.stopLockTask() }
        }
    }
}
