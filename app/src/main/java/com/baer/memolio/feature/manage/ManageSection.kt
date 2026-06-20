package com.baer.memolio.feature.manage

import androidx.annotation.StringRes
import com.baer.memolio.R

/**
 * The seven owner-management sections, in nav-rail order (spec section 7).
 * [icon] is a Material Symbols glyph name; [pro] documents which sections contain
 * Pro-gated features (the gate/upsell lives inside the page, not on the rail).
 * Mirrors the design's SECTIONS table.
 */
enum class ManageSection(@StringRes val titleRes: Int, val icon: String, val pro: Boolean) {
    Library(R.string.manage_section_library, "photo_library", pro = true),
    Playlist(R.string.manage_section_playlist, "playlist_play", pro = false),
    AddPhotos(R.string.manage_section_addphotos, "add_a_photo", pro = false),
    Appliance(R.string.manage_section_appliance, "tune", pro = true),
    Storage(R.string.manage_section_storage, "sd_storage", pro = false),
    Wallpaper(R.string.manage_section_wallpaper, "wallpaper", pro = true),
    Language(R.string.manage_section_language, "language", pro = false),
    About(R.string.manage_section_about, "info", pro = false);

    companion object {
        val default: ManageSection = Library
    }
}
