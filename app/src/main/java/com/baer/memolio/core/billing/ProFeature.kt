package com.baer.memolio.core.billing

import androidx.annotation.StringRes
import com.baer.memolio.R

/**
 * The Pro-gated feature surfaces (spec section 13, contract). Used to label upsells.
 * Holds string-resource ids (not literals) so the labels localize; [lockLabelRes] is the
 * short name shown on the inline ProLock card (it has to stay on one line even in the
 * narrow Playlist column); [titleRes] is the fuller paywall/heading name; [blurbRes] the
 * one-line description. Resolve with stringResource at the Compose call site.
 */
enum class ProFeature(
    @StringRes val titleRes: Int,
    @StringRes val blurbRes: Int,
    @StringRes val lockLabelRes: Int,
) {
    ALBUMS(R.string.profeature_albums_title, R.string.profeature_albums_blurb, R.string.profeature_albums_lock),
    APPLIANCE(R.string.profeature_appliance_title, R.string.profeature_appliance_blurb, R.string.profeature_appliance_lock),
    CUSTOM_WALLPAPER(R.string.profeature_wallpaper_title, R.string.profeature_wallpaper_blurb, R.string.profeature_wallpaper_lock),
}
