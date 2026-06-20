package com.baer.memolio.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.R
import com.baer.memolio.core.server.UploadOutcome
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

private const val BANNER_VISIBLE_MS = 3_000L

/**
 * Wraps the whole app and floats a transient top banner whenever a photo arrives over the
 * upload server, so the tablet confirms the upload no matter which screen is showing (the
 * QR/Add-photos page or the live frame). Consecutive additions coalesce into a running
 * count ("3 photos added"); duplicates and failures get their own one-line message. The
 * banner sits beneath the sleep scrim, so an asleep frame stays black.
 */
@Composable
fun UploadFeedbackOverlay(
    events: SharedFlow<UploadOutcome>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var icon by remember { mutableStateOf("check_circle") }
    var tint by remember { mutableStateOf<Color>(MemolioColors.Ok) }
    var mode by remember { mutableStateOf(UploadOutcome.ADDED) }
    var addedCount by remember { mutableIntStateOf(0) }
    // Bumped on every event; restarts the auto-dismiss timer so a burst keeps the banner up.
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(events) {
        events.collect { outcome ->
            when (outcome) {
                UploadOutcome.ADDED -> {
                    addedCount += 1
                    icon = "check_circle"; tint = MemolioColors.Ok
                }
                UploadOutcome.DUPLICATE -> { icon = "info"; tint = MemolioColors.AmberSoft }
                UploadOutcome.REJECTED -> { icon = "info"; tint = MemolioColors.Error }
            }
            mode = outcome
            visible = true
            tick += 1
        }
    }

    // Resolve the banner text in composition (not inside the effect) so resource lookup is
    // lint-clean (no LocalContext.getString) and the plural recomputes with the running count.
    val text = when (mode) {
        UploadOutcome.ADDED -> pluralStringResource(R.plurals.photos_added, addedCount, addedCount)
        UploadOutcome.DUPLICATE -> stringResource(R.string.upload_duplicate)
        UploadOutcome.REJECTED -> stringResource(R.string.upload_failed)
    }

    LaunchedEffect(tick) {
        if (tick == 0) return@LaunchedEffect
        delay(BANNER_VISIBLE_MS)
        visible = false
        addedCount = 0
    }

    Box(modifier.fillMaxSize()) {
        content()
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp)
        ) {
            Row(
                Modifier
                    .background(MemolioColors.Ink150, RoundedCornerShape(999.dp))
                    .border(1.dp, MemolioColors.BorderSoft, RoundedCornerShape(999.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .semantics { testTag = "upload_banner" },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Symbol(icon, size = 20.sp, tint = tint)
                Text(text, color = MemolioColors.TextPrimary, style = MemolioType.body)
            }
        }
    }
}
