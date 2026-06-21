package com.baer.memolio.core.datastore

enum class TransitionStyle { KEN_BURNS_CROSSFADE, CROSSFADE, SLIDE, CUT }
enum class FitMode { BLURRED_FILL, CROP, FIT_BARS }
enum class ClockStyle { DIGITAL, ANALOG }

data class PlaylistConfig(
    val activeAlbumIds: Set<String> = emptySet(),
    val shuffle: Boolean = true,
    val intervalSeconds: Int = 30,
    val transition: TransitionStyle = TransitionStyle.KEN_BURNS_CROSSFADE,
    val fitMode: FitMode = FitMode.BLURRED_FILL,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showCaption: Boolean = true,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL,
    val clockOpacity: Float = 1f,
    val clockScale: Float = 1f
)

/**
 * Full app-settings field set (all phases agree on keys here).
 * Phase 2 owns uploadToken + serverPort.
 * Phase 3/4 own wallpaperId + onboardingComplete.
 * Phase 5 owns the appliance/UI fields (sleep, kiosk, homeApp, autostart, ambientDimming, brightness, autoCleanup).
 * Phase 6 owns proUnlocked.
 */
data class AppSettings(
    val uploadToken: String = "",
    val serverPort: Int = 8080,
    val sleepEnabled: Boolean = false,
    val sleepStartMinutes: Int = 22 * 60,
    val sleepEndMinutes: Int = 7 * 60,
    val kioskEnabled: Boolean = false,
    val homeAppEnabled: Boolean = false,
    val autostartEnabled: Boolean = true,
    val ambientDimming: Boolean = true,
    val brightness: Float = 0.7f,
    val autoCleanup: Boolean = false,
    val wallpaperId: String = "default",
    val onboardingComplete: Boolean = false,
    val proUnlocked: Boolean = false,
)
