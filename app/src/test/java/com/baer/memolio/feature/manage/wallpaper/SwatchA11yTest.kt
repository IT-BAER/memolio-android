package com.baer.memolio.feature.manage.wallpaper

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioTheme
import com.baer.memolio.core.ui.WallpaperOption
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A swatch is one option in a single-choice row. The teal border is the only visual mark
 * of the active one, so the chosen state must also reach a screen reader.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class SwatchA11yTest {

    @get:Rule val composeRule = createComposeRule()

    private val option = WallpaperOption("ember", R.string.wallpaper_custom, null)

    @Test
    fun activeSwatchReportsSelected() {
        composeRule.setContent {
            MemolioTheme {
                Swatch(option, selected = true, locked = false, width = 100.dp, height = 60.dp) {}
            }
        }
        composeRule.onNodeWithText("Custom").assertIsSelected()
    }

    @Test
    fun inactiveSwatchReportsNotSelected() {
        composeRule.setContent {
            MemolioTheme {
                Swatch(option, selected = false, locked = false, width = 100.dp, height = 60.dp) {}
            }
        }
        composeRule.onNodeWithText("Custom").assertIsNotSelected()
    }
}
