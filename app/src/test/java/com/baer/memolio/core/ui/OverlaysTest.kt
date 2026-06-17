package com.baer.memolio.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class OverlaysTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clockAndDateAndCaptionRenderTheirText() {
        composeRule.setContent {
            MemolioTheme {
                Box(Modifier.fillMaxSize()) {
                    ClockOverlay(time = "14:32")
                    DateOverlay(date = "Tuesday, 16 June")
                    CaptionOverlay(text = "Sunset at the lake")
                }
            }
        }
        composeRule.onNodeWithText("14:32").assertIsDisplayed()
        composeRule.onNodeWithText("Tuesday, 16 June").assertIsDisplayed()
        composeRule.onNodeWithText("Sunset at the lake").assertIsDisplayed()
    }

    @Test
    fun wordmarkRendersUppercaseBrand() {
        composeRule.setContent { MemolioTheme { Wordmark() } }
        composeRule.onNodeWithText("MEMOLIO").assertIsDisplayed()
    }

    @Test
    fun menuButtonInvokesCallbackOnClick() {
        var clicked = false
        composeRule.setContent {
            MemolioTheme { MenuButton(onClick = { clicked = true }) }
        }
        composeRule.onNodeWithContentDescription("Open settings").performClick()
        composeRule.runOnIdle { assert(clicked) }
    }

    @Test
    fun blankCaptionRendersNothing() {
        composeRule.setContent {
            MemolioTheme { Box(Modifier.fillMaxSize()) { CaptionOverlay(text = "  ") } }
        }
        composeRule.onNodeWithText("  ").assertDoesNotExist()
    }
}
