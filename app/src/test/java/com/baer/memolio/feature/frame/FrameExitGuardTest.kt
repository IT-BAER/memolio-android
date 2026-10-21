package com.baer.memolio.feature.frame

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class FrameExitGuardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var exits = 0

    private fun showGuardAndPressBack() {
        composeRule.setContent { MemolioTheme { FrameExitGuard(onExit = { exits++ }) } }
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun backAsksBeforeLeavingTheFrame() {
        showGuardAndPressBack()
        composeRule.onNodeWithText("Close Memolio?").assertIsDisplayed()
        assertEquals(0, exits)
    }

    @Test
    fun confirmingTheDialogExits() {
        showGuardAndPressBack()
        composeRule.onNodeWithText("Close").performClick()
        assertEquals(1, exits)
    }

    @Test
    fun cancelKeepsTheFrameRunning() {
        showGuardAndPressBack()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Close Memolio?").assertDoesNotExist()
        assertEquals(0, exits)
    }
}
