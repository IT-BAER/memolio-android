package com.baer.memolio.feature.manage.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class AboutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun privacyPolicyLinkIsDisplayed() {
        composeRule.setContent {
            MemolioTheme {
                AboutScreen()
            }
        }
        composeRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
    }

    @Test
    fun legalNoticeLinkIsDisplayed() {
        composeRule.setContent {
            MemolioTheme {
                AboutScreen()
            }
        }
        composeRule.onNodeWithText("Legal Notice").assertIsDisplayed()
    }
}
