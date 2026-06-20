@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.baer.memolio.core.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.baer.memolio.R

/**
 * Material Symbols Rounded — the icon face used across Manage + Paywall. Bundled
 * offline as a subset (res/font/material_symbols_rounded.ttf) holding only the
 * glyphs Memolio uses; codepoints live in [MemolioSymbolCodepoints]. The mockup's
 * `.material-symbols-rounded` sets `wght 300`, matched here via [FontVariation].
 */
val MaterialSymbolsFamily = FontFamily(
    Font(
        R.font.material_symbols_rounded,
        variationSettings = FontVariation.Settings(FontVariation.weight(300)),
    ),
)

private val SymbolStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

/**
 * Renders a Material Symbols Rounded glyph by its name (e.g. "menu", "lock",
 * "photo_library") — the same name the mockup passes as a ligature. Sized like
 * the mockup's px `fontSize`; defaults to the ambient content color.
 */
@Composable
fun Symbol(
    name: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 24.sp,
    tint: Color = LocalContentColor.current,
) {
    val ch = MemolioSymbolCodepoints[name] ?: ' '
    Text(
        text = ch.toString(),
        modifier = modifier,
        color = tint,
        fontSize = size,
        lineHeight = size,
        fontFamily = MaterialSymbolsFamily,
        style = SymbolStyle,
    )
}
