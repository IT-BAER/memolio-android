package com.baer.memolio.feature.frame

import com.baer.memolio.core.model.Photo

/**
 * Immutable UI state for the frame. Exactly one of these renders at a time.
 * `time`/`date` are pre-formatted strings so FrameScreen stays dumb (no clock,
 * no locale logic in the view). Overlay toggles come straight from PlaylistConfig.
 */
sealed interface FrameUiState {

    /** Before the first photo set / config has resolved. */
    data object Loading : FrameUiState

    /** No live photos in the active albums → wallpaper home with live clock. */
    data class Idle(
        val time: String,
        val date: String,
        val driftPhase: Float,
        val showClock: Boolean,
        val showDate: Boolean,
        val wallpaperId: String = "default"
    ) : FrameUiState

    /** At least one photo. [currentPhoto]/[nextPhoto] feed the crossfade. */
    data class Slideshow(
        val currentPhoto: Photo,
        val nextPhoto: Photo,
        val position: Int,
        val total: Int,
        val time: String,
        val date: String,
        val showClock: Boolean,
        val showDate: Boolean,
        val showCaption: Boolean
    ) : FrameUiState {
        /** Caption is only shown when toggled on AND the photo actually has one. */
        val captionText: String?
            get() = currentPhoto.caption?.takeIf { showCaption && it.isNotBlank() }
    }
}
