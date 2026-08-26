package com.baer.memolio.core.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Symbols are icon-font glyphs in the Unicode private-use area. Left in the semantics
 * tree a screen reader tries to speak the raw codepoint, so every Symbol must be
 * decorative; the readable label always sits on the surrounding text or button.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class MemolioSymbolA11yTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun symbolGlyphIsNotExposedToAccessibility() {
        composeRule.setContent { MemolioTheme { Symbol("photo_library") } }

        val tree = composeRule.onRoot(useUnmergedTree = true).printToString(maxDepth = 100)
        // Private-use-area codepoints must not reach the semantics tree at all.
        val pua = tree.filter { it.code in 0xE000..0xF8FF }
        assertThat(pua).isEmpty()
    }
}
