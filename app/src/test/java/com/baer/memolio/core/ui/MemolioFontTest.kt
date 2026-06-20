package com.baer.memolio.core.ui

import androidx.compose.ui.text.font.FontFamily
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Guards the script-coverage decision behind [uiFontFamilyFor]: Inter's bundled subset
 * covers Latin-Extended + Cyrillic but has no CJK glyphs, so CJK locales MUST fall back to
 * the system font or they tofu. Latin/Cyrillic locales keep the brand face.
 */
class MemolioFontTest {

    @Test
    fun cjkLanguagesUseSystemFont() {
        assertThat(uiFontFamilyFor("zh")).isEqualTo(FontFamily.Default)
        assertThat(uiFontFamilyFor("ja")).isEqualTo(FontFamily.Default)
        assertThat(uiFontFamilyFor("ko")).isEqualTo(FontFamily.Default)
    }

    @Test
    fun latinAndCyrillicLanguagesKeepInter() {
        assertThat(uiFontFamilyFor("en")).isEqualTo(InterFamily)
        assertThat(uiFontFamilyFor("de")).isEqualTo(InterFamily)
        assertThat(uiFontFamilyFor("pl")).isEqualTo(InterFamily)
        assertThat(uiFontFamilyFor("ru")).isEqualTo(InterFamily)
    }

    @Test
    fun languageMatchIsCaseInsensitive() {
        assertThat(uiFontFamilyFor("ZH")).isEqualTo(FontFamily.Default)
    }
}
