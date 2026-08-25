package com.baer.memolio.feature.manage.storage

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.baer.memolio.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.component.ButtonSize
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioSwitch
import com.baer.memolio.core.ui.component.SectionHead

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var confirmEmptyTrash by remember { mutableStateOf(false) }
    val usageSub =
        if (state.totalBytes > 0) stringResource(R.string.storage_usage, formatBytes(state.usedBytes), formatBytes(state.totalBytes))
        else stringResource(R.string.storage_usage_short, formatBytes(state.usedBytes))
    Column(modifier.verticalScroll(rememberScrollState())) {
        SectionHead(title = stringResource(R.string.storage_title), sub = usageSub)

        MemolioCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), variant = CardVariant.Surface) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                StorageMeter(used = state.usedBytes, total = state.totalBytes)
                MemolioSwitch(state.autoCleanup, viewModel::setAutoCleanup, label = stringResource(R.string.storage_auto_empty))
            }
        }

        Text(
            stringResource(R.string.storage_recently_deleted, state.trash.size).uppercase(),
            color = MemolioColors.TextTertiary,
            style = MemolioType.label,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.trash.forEach { photo ->
                TrashRow(photo = photo, onRestore = { viewModel.restore(photo.id) })
            }
        }
        if (state.trash.isNotEmpty()) {
            MemolioButton(
                text = stringResource(R.string.storage_empty_trash),
                onClick = { confirmEmptyTrash = true },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Sm,
                icon = "delete_forever",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        // Clearance so the last row can scroll fully above the floating play FAB.
        Spacer(Modifier.height(96.dp))
    }

    if (confirmEmptyTrash) {
        EmptyTrashDialog(
            count = state.trash.size,
            onConfirm = { confirmEmptyTrash = false; viewModel.emptyTrash() },
            onDismiss = { confirmEmptyTrash = false }
        )
    }
}

/** Confirms emptying the trash. The photos and their files go for good, so this cannot be undone. */
@Composable
private fun EmptyTrashDialog(count: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MemolioColors.SurfaceCard,
        title = { Text(stringResource(R.string.storage_empty_trash_title), color = MemolioColors.TextPrimary, style = MemolioType.h3) },
        text = {
            Text(
                stringResource(R.string.storage_empty_trash_body, count),
                color = MemolioColors.TextSecondary,
                style = MemolioType.body
            )
        },
        confirmButton = {
            MemolioButton(
                text = stringResource(R.string.storage_empty_trash),
                onClick = onConfirm,
                variant = ButtonVariant.Secondary,
                size = ButtonSize.Sm,
                icon = "delete_forever"
            )
        },
        dismissButton = {
            MemolioButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Sm
            )
        }
    )
}

/** The storage usage bar: a faint full-width track with a teal→amber gradient fill. */
@Composable
private fun StorageMeter(used: Long, total: Long) {
    val fraction = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
    Box(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MemolioColors.SurfaceGlassStrong)
    ) {
        if (fraction > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Brush.horizontalGradient(listOf(MemolioColors.Teal, MemolioColors.AmberVivid)))
            )
        }
    }
}

@Composable
private fun TrashRow(photo: Photo, onRestore: () -> Unit) {
    MemolioCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Surface,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = photo.thumbPath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                )
                Text(
                    // Never the photo id: a 32-character hash is unreadable and leaks an
                    // internal key. The caption identifies the photo when there is one,
                    // otherwise the deletion date does.
                    text = photo.caption?.takeIf { it.isNotBlank() } ?: deletedLabel(photo),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            MemolioButton(
                text = stringResource(R.string.storage_restore),
                onClick = onRestore,
                variant = ButtonVariant.Quiet,
                size = ButtonSize.Sm,
                icon = "restore_from_trash"
            )
        }
    }
}

/** Locale-aware "deleted on ..." label, used when a trashed photo carries no caption. */
@Composable
private fun deletedLabel(photo: Photo): String {
    val context = LocalContext.current
    val at = photo.deletedAt ?: return ""
    return DateUtils.formatDateTime(
        context,
        at,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    return "%.1f GB".format(mb / 1024.0)
}
