package com.baer.memolio.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class MemolioThemeTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun themeExposesDarkLeaningBackground() {
        val captured = mutableStateOf<Color?>(null)
        composeRule.setContent {
            MemolioTheme {
                captured.value = MaterialTheme.colorScheme.background
            }
        }
        composeRule.runOnIdle {
            val bg = captured.value!!
            // dark-leaning: each channel below mid-grey
            assertThat(bg.red).isLessThan(0.2f)
            assertThat(bg.green).isLessThan(0.2f)
            assertThat(bg.blue).isLessThan(0.2f)
        }
    }

    @Test
    fun frameInkTokenIsNearWhite() {
        assertThat(MemolioInk.red).isGreaterThan(0.9f)
        assertThat(MemolioInk.green).isGreaterThan(0.9f)
        assertThat(MemolioInk.blue).isGreaterThan(0.85f)
    }
}
