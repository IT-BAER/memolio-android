package com.baer.memolio.feature.manage

/**
 * The seven owner-management sections, in nav-rail order (spec section 7).
 * [icon] is a Material Symbols glyph name; [pro] marks Pro-gated sections (a lock
 * shows in the rail until purchased). Mirrors the design's SECTIONS table.
 */
enum class ManageSection(val title: String, val icon: String, val pro: Boolean) {
    Library("Library", "photo_library", pro = true),
    Playlist("Playlist", "playlist_play", pro = false),
    AddPhotos("Add photos", "add_a_photo", pro = false),
    Appliance("Appliance", "tune", pro = true),
    Storage("Storage", "sd_storage", pro = false),
    Wallpaper("Wallpaper", "wallpaper", pro = true),
    About("About", "info", pro = false);

    companion object {
        val default: ManageSection = Library
    }
}
