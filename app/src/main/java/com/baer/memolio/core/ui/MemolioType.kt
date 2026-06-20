@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.baer.memolio.core.ui

import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.baer.memolio.R

/**
 * Inter — the Memolio brand face, bundled offline as a single variable font
 * (res/font/inter_variable.ttf). Each [Font] pins a weight via [FontVariation]
 * so the variable axis renders the exact design weights (Thin 200 → SemiBold
 * 600). minSdk 26 satisfies variable-font support.
 *
 * Loaded locally — NEVER from Google Fonts (offline mandate). The design's
 * @font-face CDN srcs were swapped for this bundled binary, exactly as the
 * design file's own note instructs.
 */
val InterFamily = FontFamily(
    Font(R.font.inter_variable, FontWeight.Thin, variationSettings = FontVariation.Settings(FontVariation.weight(200))),
    Font(R.font.inter_variable, FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.inter_variable, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter_variable, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.inter_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

/**
 * The family used for **localizable** UI text (everything except the ASCII clock numerals
 * and the Latin "MEMOLIO" wordmark). Inter's bundled subset covers Latin-Extended + Cyrillic
 * (verified: en/de/fr/it/nl/pl/pt/es/ru all render), but has **no CJK glyphs**, so Chinese
 * would render as tofu. For CJK locales we fall back to the system font ([FontFamily.Default],
 * which resolves to the device's Noto Sans CJK). [MemolioTheme] sets this from the active
 * configuration locale on each composition; a per-app language change recreates the Activity,
 * so the whole tree recomposes and the text tiers below (getters) pick up the new family.
 */
internal var activeUiFontFamily: FontFamily = InterFamily

/** Languages whose script Inter's subset cannot cover; routed to the system font. */
private val SYSTEM_FONT_LANGUAGES = setOf("zh", "ja", "ko")

/** The UI font family for [language] (ISO-639 primary subtag): system for CJK, else Inter. */
fun uiFontFamilyFor(language: String): FontFamily =
    if (language.lowercase(Locale.ROOT) in SYSTEM_FONT_LANGUAGES) FontFamily.Default else InterFamily

/** The big soft drop under the hero clock — `--shadow-clock`. */
val MemolioClockShadow = Shadow(
    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.42f),
    offset = Offset(0f, 32f),
    blurRadius = 96f,
)

/**
 * Named text styles, 1:1 with the design type scale. Display tiers serve the
 * always-on frame (very large clock); UI tiers serve the Manage app.
 */
object MemolioType {
    /** The hero clock: 168sp, Thin, 0.82 line-height, soft drop. */
    val clock = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Thin,
        fontSize = 168.sp,
        lineHeight = 138.sp, // 0.82 * 168
        letterSpacing = 0.em,
        fontFeatureSettings = "tnum", // tabular numerals — design clock uses tabular-nums (no per-minute jitter)
        shadow = MemolioClockShadow,
    )

    /** Hero numerals / marketing / paywall price: 88sp Thin. */
    val display = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Thin, fontSize = 88.sp, lineHeight = 96.8.sp)

    // Localizable text tiers resolve [activeUiFontFamily] at access time (getters), so a
    // CJK locale picks up the system font and never tofus. The clock/display/wordmark above
    // and below stay on Inter (ASCII numerals + Latin brand), so the design face is preserved.

    /** Date line / large screen titles: 40sp. */
    val h1 get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.Light, fontSize = 40.sp, lineHeight = 48.sp)

    /** Section heads / captions: 28sp SemiBold. */
    val h2 get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp)

    /** Card titles / wordmark: 22sp SemiBold. */
    val h3 get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)

    val bodyLg get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp)
    val body get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp)
    val sm get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val xs get() = TextStyle(fontFamily = activeUiFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)

    /** Uppercase eyebrow / small-caps label: 12sp Medium, 0.04em tracking. */
    val label get() = TextStyle(
        fontFamily = activeUiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.04.em,
    )

    /** Uppercase MEMOLIO wordmark: Medium, 0.18em tracking (size set by caller). */
    val wordmark = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.18.em,
        lineHeight = 1.em,
    )
}
