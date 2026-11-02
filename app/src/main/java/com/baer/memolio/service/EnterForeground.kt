package com.baer.memolio.service

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build

/**
 * Runs [start] (a `startForeground` call) and reports whether the service is foreground.
 * Android 12+ refuses it when the OS recreates the service in the background (sticky
 * restart, a WorkManager job starting the process); then [onDenied] runs instead of a crash.
 */
fun enterForeground(start: () -> Unit, onDenied: () -> Unit): Boolean =
    try {
        start()
        true
    } catch (e: IllegalStateException) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || e !is ForegroundServiceStartNotAllowedException) throw e
        onDenied()
        false
    }
