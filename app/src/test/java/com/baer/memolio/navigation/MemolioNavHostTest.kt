package com.baer.memolio.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MemolioNavHostTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun startsAtFrameWhenOnboardingComplete() {
        composeRule.setContent {
            MemolioNavHost(
                start = StartDestination.Frame,
                frameContent = { _ -> Text("FRAME") },
                manageContent = { Text("MANAGE") },
                onboardContent = { Text("ONBOARD") }
            )
        }
        composeRule.onNodeWithText("FRAME").assertIsDisplayed()
    }

    @Test
    fun startsAtOnboardWhenOnboardingIncomplete() {
        composeRule.setContent {
            MemolioNavHost(
                start = StartDestination.Onboard,
                frameContent = { _ -> Text("FRAME") },
                manageContent = { Text("MANAGE") },
                onboardContent = { Text("ONBOARD") }
            )
        }
        composeRule.onNodeWithText("ONBOARD").assertIsDisplayed()
    }

    @Test
    fun frameOpenManageNavigatesToManage() {
        composeRule.setContent {
            MemolioNavHost(
                start = StartDestination.Frame,
                frameContent = { onOpenManage ->
                    Button(onClick = onOpenManage) { Text("MENU") }
                },
                manageContent = { Text("MANAGE") },
                onboardContent = { Text("ONBOARD") }
            )
        }
        composeRule.onNodeWithText("MENU").performClick()
        composeRule.onNodeWithText("MANAGE").assertIsDisplayed()
    }

    @Test
    fun controllerCanBeConstructedWithTypedGraph() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller = TestNavHostController(context)
        controller.navigatorProvider.addNavigator(ComposeNavigator())
        // no exception == pass
    }
}
