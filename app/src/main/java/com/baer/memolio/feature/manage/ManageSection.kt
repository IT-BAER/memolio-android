package com.baer.memolio.feature.manage

/** The seven owner-management sections, in nav-rail order (spec section 7). */
enum class ManageSection(val title: String) {
    Library("Library"),
    Playlist("Playlist"),
    AddPhotos("Add photos"),
    Appliance("Appliance"),
    Storage("Storage"),
    Wallpaper("Wallpaper"),
    About("About");

    companion object {
        val default: ManageSection = Library
    }
}
