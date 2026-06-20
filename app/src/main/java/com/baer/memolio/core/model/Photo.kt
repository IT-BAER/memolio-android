package com.baer.memolio.core.model

data class Photo(
    val id: String,
    val originalPath: String,
    val displayCachePath: String,
    val thumbPath: String,
    val contentHash: String,
    val width: Int,
    val height: Int,
    val orientation: Int,
    val caption: String?,
    val albumId: String,
    val favorite: Boolean,
    val sortOrder: Int,
    val addedAt: Long,
    val sourceDevice: String?,
    val deletedAt: Long?,
    /** When false the photo is hidden from the slideshow but stays in the library. */
    val inPlaylist: Boolean = true
) {
    val isInTrash: Boolean get() = deletedAt != null
}
