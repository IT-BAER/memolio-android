package com.baer.memolio.feature.frame

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.baer.memolio.R

/**
 * Back on the frame asks before [onExit]: one stray press would otherwise close the activity
 * and leave the tablet on the launcher. Back again while asking dismisses the dialog.
 */
@Composable
fun FrameExitGuard(onExit: () -> Unit) {
    var asking by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = !asking) { asking = true }
    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(stringResource(R.string.frame_exit_title)) },
            text = { Text(stringResource(R.string.frame_exit_body)) },
            confirmButton = {
                TextButton(onClick = {
                    asking = false
                    onExit()
                }) { Text(stringResource(R.string.frame_exit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { asking = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
