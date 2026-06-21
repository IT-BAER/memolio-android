package com.baer.memolio.assets

import org.junit.Test
import java.io.File

class UploadHtmlLegalLinksTest {

    private val PRIVACY_URL =
        "https://github.com/IT-BAER/memolio-android/blob/main/docs/privacy-policy.md"
    private val LEGAL_URL =
        "https://github.com/IT-BAER/memolio-android/blob/main/docs/legal-notice.md"

    private fun readUploadHtml(): String {
        // Working dir for unit tests is the app/ module root.
        val moduleRoot = File("src/main/assets/upload.html")
        if (moduleRoot.exists()) return moduleRoot.readText()
        // Fallback: locate relative to this class file's location via project structure.
        val projectRoot = File(System.getProperty("user.dir") ?: ".")
        val fromProject = File(projectRoot, "src/main/assets/upload.html")
        if (fromProject.exists()) return fromProject.readText()
        error("upload.html not found; searched ${moduleRoot.absolutePath} and ${fromProject.absolutePath}")
    }

    @Test
    fun uploadHtmlContainsPrivacyUrl() {
        val text = readUploadHtml()
        assert(text.contains(PRIVACY_URL)) {
            "upload.html does not contain privacy URL: $PRIVACY_URL"
        }
    }

    @Test
    fun uploadHtmlContainsLegalUrl() {
        val text = readUploadHtml()
        assert(text.contains(LEGAL_URL)) {
            "upload.html does not contain legal URL: $LEGAL_URL"
        }
    }
}
