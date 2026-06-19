package com.baer.memolio.core.billing

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.baer.memolio.core.ui.ProGate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProGateTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun isProTrueRendersUnlockedSlot() {
        composeRule.setContent {
            ProGate(
                feature = ProFeature.ALBUMS,
                isPro = true,
                onUpsell = {},
                locked = { Text("LOCKED") },
                unlocked = { Text("UNLOCKED") }
            )
        }
        composeRule.onNodeWithText("UNLOCKED").assertIsDisplayed()
    }

    @Test
    fun isProFalseRendersLockedSlot() {
        composeRule.setContent {
            ProGate(
                feature = ProFeature.ALBUMS,
                isPro = false,
                onUpsell = {},
                locked = { Text("LOCKED") },
                unlocked = { Text("UNLOCKED") }
            )
        }
        composeRule.onNodeWithText("LOCKED").assertIsDisplayed()
    }

    @Test
    fun lockedSlotCanTriggerUpsell() {
        var upsellOpened = false
        composeRule.setContent {
            ProGate(
                feature = ProFeature.CUSTOM_WALLPAPER,
                isPro = false,
                onUpsell = { upsellOpened = true },
                unlocked = { Text("UNLOCKED") }
            )
        }
        composeRule.onNodeWithText(ProFeature.CUSTOM_WALLPAPER.title, substring = true).performClick()
        assert(upsellOpened)
    }
}
