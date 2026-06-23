package com.baer.memolio.feature.frame

import com.baer.memolio.core.datastore.ClockStyle
import com.baer.memolio.core.datastore.FitMode
import com.baer.memolio.core.datastore.TransitionStyle
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
        val wallpaperId: String = "default",
        val customWallpaperPath: String? = null,
        val clockStyle: ClockStyle = ClockStyle.DIGITAL,
        val hour: Int = 0,
        val minute: Int = 0,
        val clockOpacity: Float = 1f,
        val clockScale: Float = 1f
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
        val showCaption: Boolean,
        val clockStyle: ClockStyle = ClockStyle.DIGITAL,
        val fitMode: FitMode = FitMode.BLURRED_FILL,
        val transition: TransitionStyle = TransitionStyle.KEN_BURNS_CROSSFADE,
        /** Direction of the last cursor move: true = forward (next/auto), false = backward
         *  (previous). Drives the SLIDE transition so a swipe-right animates left-to-right. */
        val advanceForward: Boolean = true,
        val paused: Boolean = false,
        val hour: Int = 0,
        val minute: Int = 0,
        val clockOpacity: Float = 1f,
        val clockScale: Float = 1f
    ) : FrameUiState {
        /** Caption is only shown when toggled on AND the photo actually has one. */
        val captionText: String?
            get() = currentPhoto.caption?.takeIf { showCaption && it.isNotBlank() }
    }
}
