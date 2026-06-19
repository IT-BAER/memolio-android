package com.baer.memolio.navigation

import kotlinx.serialization.Serializable

/** Slideshow + idle home (Phase 3 owns the screen; route declared here for the NavHost). */
@Serializable
data object FrameRoute

/** First-run wizard (Phase 4). */
@Serializable
data object OnboardRoute

/** Management graph root. */
@Serializable
data object ManageRoute

@Serializable
data object LibraryRoute

@Serializable
data object PlaylistRoute

@Serializable
data object AddPhotosRoute

@Serializable
data object ApplianceRoute

@Serializable
data object StorageRoute

@Serializable
data object WallpaperRoute

@Serializable
data object AboutRoute
