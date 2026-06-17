package com.baer.memolio.core.datastore

enum class TransitionStyle { KEN_BURNS_CROSSFADE, CROSSFADE, SLIDE, CUT }
enum class FitMode { BLURRED_FILL, CROP, FIT_BARS }

data class PlaylistConfig(
    val activeAlbumIds: Set<String> = emptySet(),
    val shuffle: Boolean = true,
    val intervalSeconds: Int = 30,
    val transition: TransitionStyle = TransitionStyle.KEN_BURNS_CROSSFADE,
    val fitMode: FitMode = FitMode.BLURRED_FILL,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showCaption: Boolean = true
)
