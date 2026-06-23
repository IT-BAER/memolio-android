package com.baer.memolio.feature.frame

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
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
    fun slideshowState_hidesWordmark() {
        // The wordmark is idle-only; a photo slideshow stays clean.
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithText("MEMOLIO").assertDoesNotExist()
    }

    @Test
    fun slideshowState_hidesMenuButtonByDefault() {
        // In a slideshow the menu button is hidden until a tap reveals it (the timed
        // tap-reveal itself is verified on-device; here we guard the default-hidden state).
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {})
            }
        }
        composeRule.onNodeWithContentDescription("Open settings").assertDoesNotExist()
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

    @Test
    fun analogStyleRendersDialNotDigits() {
        val state = FrameUiState.Slideshow(
            currentPhoto = photo("p1"), nextPhoto = photo("p2"),
            position = 1, total = 1, time = "14:32", date = "Tuesday, 16 June",
            showClock = true, showDate = false, showCaption = false,
            clockStyle = com.baer.memolio.core.datastore.ClockStyle.ANALOG,
            hour = 14, minute = 32
        )
        composeRule.setContent { MemolioTheme { FrameScreen(state = state, onOpenManage = {}) } }
        composeRule.onNodeWithContentDescription("14:32").assertIsDisplayed()
        composeRule.onNodeWithText("14:32").assertDoesNotExist()
    }

    // ---- Gesture commands -------------------------------------------------------

    @Test
    fun swipeLeft_invokesOnNext() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {}, onNext = { invoked = true })
            }
        }
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assert(invoked) { "onNext was not invoked after swipe left" }
    }

    @Test
    fun swipeRight_invokesOnPrevious() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {}, onPrevious = { invoked = true })
            }
        }
        composeRule.onRoot().performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        assert(invoked) { "onPrevious was not invoked after swipe right" }
    }

    @Test
    fun click_invokesOnTogglePause() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = {}, onTogglePause = { invoked = true })
            }
        }
        composeRule.onRoot().performTouchInput { click() }
        composeRule.waitForIdle()
        assert(invoked) { "onTogglePause was not invoked after click" }
    }

    @Test
    fun afterReveal_favoriteButtonClick_invokesOnToggleFavorite() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(
                    state = slideshowState,
                    onOpenManage = {},
                    onToggleFavorite = { invoked = true }
                )
            }
        }
        // First click to reveal controls (also triggers onTogglePause, which is a no-op here).
        composeRule.onRoot().performTouchInput { click() }
        composeRule.waitForIdle()
        // Favorite button should now be visible.
        composeRule.onNodeWithContentDescription("Add to favorites").performClick()
        composeRule.waitForIdle()
        assert(invoked) { "onToggleFavorite was not invoked after favorite button click" }
    }

    @Test
    fun slideshowState_longPress_invokesOnOpenManage() {
        var invoked = false
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = slideshowState, onOpenManage = { invoked = true })
            }
        }
        composeRule.onRoot().performTouchInput { longClick() }
        composeRule.waitForIdle()
        assert(invoked) { "onOpenManage was not invoked after long-press on slideshow" }
    }

    // ---- Transition styles ------------------------------------------------------

    @Test
    fun slideshow_rendersForEveryTransitionStyle() {
        // Each TransitionStyle drives a different render branch (Crossfade / AnimatedContent /
        // instant). The clock overlay is transition-independent, so it must display in all of
        // them — this guards every branch from crashing under the LEGACY renderer. (Pixel
        // behavior of the animations is deferred to Roborazzi on Linux CI.)
        val styles = com.baer.memolio.core.datastore.TransitionStyle.values()
        val stateHolder = androidx.compose.runtime.mutableStateOf(
            slideshowState.copy(transition = styles.first())
        )
        composeRule.setContent {
            MemolioTheme {
                FrameScreen(state = stateHolder.value, onOpenManage = {})
            }
        }
        styles.forEach { t ->
            composeRule.runOnUiThread { stateHolder.value = slideshowState.copy(transition = t) }
            composeRule.waitForIdle()
            composeRule.onNodeWithText("14:32").assertIsDisplayed()
        }
    }
}
