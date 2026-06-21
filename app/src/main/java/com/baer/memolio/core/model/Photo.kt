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
    val inPlaylist: Boolean = true,
    /** Normalized face/subject focal point (0..1 each). Null when not yet detected. */
    val focalX: Float? = null,
    val focalY: Float? = null
) {
    val isInTrash: Boolean get() = deletedAt != null
}
