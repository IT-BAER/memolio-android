package com.baer.memolio.core.billing

/**
 * The Pro-gated feature surfaces (spec section 13, contract). Used to label upsells.
 * [lockLabel] is the short name shown on the inline ProLock card (it has to stay on one
 * line even in the narrow Playlist column); [title] is the fuller paywall/heading name.
 */
enum class ProFeature(val title: String, val blurb: String, val lockLabel: String) {
    ALBUMS("Albums & playlists", "Organize photos into albums and choose which ones cycle.", "Albums"),
    APPLIANCE("Appliance mode", "Auto-start, kiosk lock, Home app, scheduled sleep, and ambient dimming.", "Appliance suite"),
    CUSTOM_WALLPAPER("Custom wallpapers", "Use alternate and custom wallpapers beyond the default.", "Custom wallpaper")
}
