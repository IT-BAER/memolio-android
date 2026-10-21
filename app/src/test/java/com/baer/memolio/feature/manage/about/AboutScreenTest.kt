package com.baer.memolio.feature.manage.about

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class AboutScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showAbout() {
        composeRule.setContent {
            MemolioTheme {
                // Production hosts About inside the scrolling Settings column.
                Column(Modifier.verticalScroll(rememberScrollState())) { AboutScreen() }
            }
        }
    }

    private fun clickAndCaptureIntent(label: String): Intent {
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        val started = shadowOf(composeRule.activity).nextStartedActivity
        assertNotNull("tapping \"$label\" must start an activity", started)
        return started
    }

    @Test
    fun privacyPolicyLinkIsDisplayed() {
        showAbout()
        composeRule.onNodeWithText("Privacy Policy").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun legalNoticeLinkIsDisplayed() {
        showAbout()
        composeRule.onNodeWithText("Legal Notice").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun rateOpensThePlayListingOfTheReleasePackage() {
        showAbout()
        val intent = clickAndCaptureIntent("Rate on Google Play")
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("market://details?id=com.baer.memolio", intent.dataString)
    }

    @Test
    fun shareOpensAChooserWithThePlayLink() {
        showAbout()
        val chooser = clickAndCaptureIntent("Share Memolio")
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        val text = send.getStringExtra(Intent.EXTRA_TEXT)!!
        assert(text.contains("https://play.google.com/store/apps/details?id=com.baer.memolio")) { text }
    }

    @Test
    fun feedbackOpensAMailToTheDeveloper() {
        showAbout()
        val intent = clickAndCaptureIntent("Send feedback")
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto:admin@it-baer.net", intent.dataString)
        assert(intent.getStringExtra(Intent.EXTRA_SUBJECT)!!.startsWith("Memolio")) {
            intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
        }
    }
}
