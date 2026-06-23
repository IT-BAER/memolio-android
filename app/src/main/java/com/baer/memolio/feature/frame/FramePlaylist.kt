package com.baer.memolio.feature.frame

import kotlin.random.Random

/**
 * Pure, immutable slideshow cursor. No Android, no coroutines — just the order of
 * photo ids and a current index, so every advance/shuffle/wrap rule is unit-tested
 * in isolation. [FrameViewModel] holds one of these and rebuilds it when the photo
 * set or config changes; the ticker calls [advanced] to move forward.
 */
data class FramePlaylist(
    val order: List<String>,
    val index: Int
) {
    val currentId: String? get() = order.getOrNull(index)

    /** Peeks the next id, wrapping at the end. Single-photo playlists peek themselves. */
    val nextId: String?
        get() = if (order.isEmpty()) null else order[(index + 1) % order.size]

    /** Human-readable 1-based position; 0 when empty. */
    val position: Int get() = if (order.isEmpty()) 0 else index + 1
    val size: Int get() = order.size

    /** Returns a new cursor advanced by one, wrapping at the end. */
    fun advanced(): FramePlaylist {
        if (order.isEmpty()) return this
        return copy(index = (index + 1) % order.size)
    }

    /** Returns a new cursor stepped back by one, wrapping at the start. */
    fun rewound(): FramePlaylist {
        if (order.isEmpty()) return this
        return copy(index = (index - 1 + order.size) % order.size)
    }

    companion object {
        fun create(ids: List<String>, shuffle: Boolean, seed: Long): FramePlaylist {
            val order = if (shuffle) ids.shuffled(Random(seed)) else ids
            return FramePlaylist(order = order, index = 0)
        }
    }
}
