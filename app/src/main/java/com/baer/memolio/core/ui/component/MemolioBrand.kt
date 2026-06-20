package com.baer.memolio.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.MemolioWallpaper
import com.baer.memolio.core.ui.OverlayScrim

/** Wordmark tone, mirroring the design's `tone` prop. */
enum class WordmarkTone { Faint, Solid, Ink, Teal }

/**
 * The Memolio wordmark — uppercase "MEMOLIO" with wide 0.18em tracking, Medium
 * weight. Defaults to [WordmarkTone.Faint] (the frame overlay); [WordmarkTone.Solid]
 * for full-strength paper on cards/rails.
 */
@Composable
fun MemolioWordmark(
    modifier: Modifier = Modifier,
    tone: WordmarkTone = WordmarkTone.Faint,
    size: TextUnit = 22.sp,
) {
    val color = when (tone) {
        WordmarkTone.Faint -> MemolioColors.Paper340
        WordmarkTone.Solid -> MemolioColors.Paper
        WordmarkTone.Ink -> MemolioColors.Ink000
        WordmarkTone.Teal -> MemolioColors.Teal
    }
    Text(
        text = "MEMOLIO",
        modifier = modifier,
        color = color,
        fontSize = size,
        style = MemolioType.wordmark,
    )
}

/**
 * The reusable Memolio wallpaper backdrop — the warm-dark layered gradient stack
 * painted as pure vector ([MemolioWallpaper]), with the optional legibility scrim
 * and [content] composed on top. The design-system `AmbientBackground`.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    scrim: Boolean = true,
    driftPhase: Float = 0f,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        MemolioWallpaper(modifier = Modifier.fillMaxSize(), driftPhase = driftPhase)
        if (scrim) OverlayScrim()
        content()
    }
}
