package com.baer.memolio.core.billing

/** The Pro-gated feature surfaces (spec section 13, contract). Used to label upsells. */
enum class ProFeature(val title: String, val blurb: String) {
    ALBUMS("Albums & playlists", "Organize photos into albums and choose which ones cycle."),
    APPLIANCE("Appliance mode", "Auto-start, kiosk lock, Home app, scheduled sleep, and ambient dimming."),
    CUSTOM_WALLPAPER("Custom wallpapers", "Use alternate and custom wallpapers beyond the default.")
}
