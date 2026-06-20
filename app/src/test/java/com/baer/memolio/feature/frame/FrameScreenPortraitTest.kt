package com.baer.memolio.feature.frame

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Portrait-orientation render checks for [FrameScreen]. Confirms the portrait-first
 * layout path (BoxWithConstraints → short-edge metrics → portrait scrim) renders the
 * overlays without crashing on a tall (9:16-ish) tablet. Behavior-only (LEGACY graphics);
 * pixel baselines are deferred to Linux CI.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34], qualifiers = "w800dp-h1280dp-port-xhdpi")
class FrameScreenPortraitTest {

    @get:Rule val composeRule = createComposeRule()

    private val idle = FrameUiState.Idle(
        time = "14:32",
        date = "Monday, 16 June",
        driftPhase = 0f,
        showClock = true,
        showDate = true
    )

    private fun photo(id: String, caption: String?) = Photo(
        id = id, originalPath = "/o/$id.jpg", displayCachePath = "/d/$id.jpg",
        thumbPath = "/t/$id.jpg", contentHash = id, width = 1080, height = 1920,
        orientation = 0, caption = caption, albumId = "a1", favorite = false,
        sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = null
    )

    private val slideshow = FrameUiState.Slideshow(
        currentPhoto = photo("p1", "Lake Bled · 2023"),
        nextPhoto = photo("p2", null),
        position = 0,
        total = 1,
        time = "14:32",
        date = "Monday, 16 June",
        showClock = true,
        showDate = true,
        showCaption = true
    )

    @Test
    fun idlePortrait_showsClockDateWordmarkMenu() {
        composeRule.setContent { MemolioTheme { FrameScreen(state = idle, onOpenManage = {}) } }
        composeRule.onNodeWithText("14:32").assertIsDisplayed()
        composeRule.onNodeWithText("Monday, 16 June").assertIsDisplayed()
        composeRule.onNodeWithText("MEMOLIO").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open settings").assertIsDisplayed()
    }

    @Test
    fun slideshowPortrait_showsCaption() {
        composeRule.setContent { MemolioTheme { FrameScreen(state = slideshow, onOpenManage = {}) } }
        composeRule.onNodeWithText("Lake Bled · 2023").assertIsDisplayed()
    }
}
