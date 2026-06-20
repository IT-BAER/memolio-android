@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.baer.memolio.core.ui

import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
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

    /** Date line / large screen titles: 40sp. */
    val h1 = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Light, fontSize = 40.sp, lineHeight = 48.sp)

    /** Section heads / captions: 28sp SemiBold. */
    val h2 = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp)

    /** Card titles / wordmark: 22sp SemiBold. */
    val h3 = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)

    val bodyLg = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp)
    val body = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp)
    val sm = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val xs = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)

    /** Uppercase eyebrow / small-caps label: 12sp Medium, 0.04em tracking. */
    val label = TextStyle(
        fontFamily = InterFamily,
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
