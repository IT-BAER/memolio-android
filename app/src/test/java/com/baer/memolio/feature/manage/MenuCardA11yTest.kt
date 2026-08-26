package com.baer.memolio.feature.manage

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithText
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A rail card is one tap target carrying its own label. It must also report the button
 * role, or a screen reader announces the word alone with no hint that it can be tapped.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class MenuCardA11yTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cardIsALabelledButton() {
        composeRule.setContent { MemolioTheme { MenuCard(ManageSection.Library) {} } }
        composeRule.onNodeWithText("Library")
            .assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }
}
