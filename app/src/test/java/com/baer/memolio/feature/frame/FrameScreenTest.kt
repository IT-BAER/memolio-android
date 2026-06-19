package com.baer.memolio.feature.frame

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Semantics/behavior tests for [FrameScreen]. No pixel screenshots (deferred to
 * Linux/CI where Robolectric NATIVE graphics is available). These run with the LEGACY
 * renderer which supports semantics but not hardware-accelerated pixel rendering.
 *
 * Tests feed [FrameScreen] with fixed [FrameUiState] instances directly — no ViewModel.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class FrameScreenTest {

    @get:Rule val composeRule = createComposeRule()

    // ---- helpers ----------------------------------------------------------------

    private fun photo(
        id: String = "p1",
        caption: String? = null
    ) = Photo(
        id = id,
        originalPath = "/o/$id.jpg",
        displayCachePath = "/d/$id.jpg",
        thumbPath = "/t/$id.jpg",
        contentHash = id,
        width = 1920,
        height = 1080,
        orientation = 0,
        caption = caption,
        albumId = "a1",
        favorite = false,
        sortOrder = 0,
        addedAt = 0L,
        sourceDevice = null,
        deletedAt = null
    )

    private val idleState = FrameUiState.Idle(
        time = "14:32",
        date = "Monday, 16 June",
        driftPhase = 0f,
        showClock = true,
        showDate = true
    )

    private val slideshowState = FrameUiState.Slideshow(
        currentPhoto = photo("p1", caption = "Sunset at the lake"),
        nextPhoto = photo("p2"),
        position = 0,
        total = 2,
        time = "14:32",
        date = "Monday, 16 June",
        showClock = true,
        showDate = true,
        showCaption = true
    )

    // ---- Loading state ----------------------------------------------------------

    @Test
    fun loadingState_rendersWithoutCrash() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = FrameUiState.Loading, onOpenManage = {})
            }
        }
        composeRule.waitForIdle()
        // No crash and no overlay content rendered.
        composeRule.onNodeWithText("14:32").assertDoesNotExist()
        composeRule.onNodeWithText("MEMOLIO").assertDoesNotExist()
    }

    // ---- Idle state -------------------------------------------------------------

    @Test
    fun idleState_showsWordmark() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = idleState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("MEMOLIO").assertIsDisplayed()
    }

    @Test
    fun idleState_showsClockWhenEnabled() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = idleState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("14:32").assertIsDisplayed()
    }

    @Test
    fun idleState_hidesClockWhenDisabled() {
        val state = idleState.copy(showClock = false)
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = state, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("14:32").assertDoesNotExist()
    }

    @Test
    fun idleState_showsDateWhenEnabled() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = idleState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("Monday, 16 June").assertIsDisplayed()
    }

    @Test
    fun idleState_hidesDateWhenDisabled() {
        val state = idleState.copy(showDate = false)
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = state, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("Monday, 16 June").assertDoesNotExist()
    }

    @Test
    fun idleState_menuButtonHasCorrectContentDescription() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = idleState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithContentDescription("Open settings").assertIsDisplayed()
    }

    @Test
    fun idleState_menuButtonClick_invokesOnOpenManage() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = idleState, onOpenManage = { invoked = true })
            }
        }
        composeRule.onNodeWithContentDescription("Open settings").performClick()
        composeRule.waitForIdle()
        assert(invoked) { "onOpenManage was not invoked after menu button click" }
    }

    @Test
    fun idleState_longPress_invokesOnOpenManage() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = idleState, onOpenManage = { invoked = true })
            }
        }
        // Long-press anywhere on the root triggers the kiosk-safe fallback.
        composeRule.onNodeWithContentDescription("Open settings", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("MEMOLIO").performTouchInput { longClick() }
        composeRule.waitForIdle()
        assert(invoked) { "onOpenManage was not invoked after long-press" }
    }

    // ---- Slideshow state --------------------------------------------------------

    @Test
    fun slideshowState_showsClockWhenEnabled() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("14:32").assertIsDisplayed()
    }

    @Test
    fun slideshowState_hidesClockWhenDisabled() {
        val state = slideshowState.copy(showClock = false)
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = state, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("14:32").assertDoesNotExist()
    }

    @Test
    fun slideshowState_showsCaptionWhenEnabledAndPresent() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("Sunset at the lake").assertIsDisplayed()
    }

    @Test
    fun slideshowState_hidesCaptionWhenToggleOff() {
        val state = slideshowState.copy(showCaption = false)
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = state, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("Sunset at the lake").assertDoesNotExist()
    }

    @Test
    fun slideshowState_hidesCaptionWhenPhotoHasNone() {
        val state = slideshowState.copy(
            currentPhoto = photo("p1", caption = null),
            showCaption = true
        )
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = state, onOpenManage = {})
            }
        }
        // No caption text node should exist.
        composeRule.onNodeWithText("Sunset at the lake").assertDoesNotExist()
    }

    @Test
    fun slideshowState_showsWordmark() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("MEMOLIO").assertIsDisplayed()
    }

    @Test
    fun slideshowState_menuButtonClick_invokesOnOpenManage() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = { invoked = true })
            }
        }
        composeRule.onNodeWithContentDescription("Open settings").performClick()
        composeRule.waitForIdle()
        assert(invoked) { "onOpenManage was not invoked after menu button click in slideshow" }
    }

    @Test
    fun slideshowState_doesNotShowWordmarkCaptionInLoadingState() {
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = FrameUiState.Loading, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("MEMOLIO").assertDoesNotExist()
        composeRule.onNodeWithText("Sunset at the lake").assertDoesNotExist()
    }
}
