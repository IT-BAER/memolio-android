package com.baer.memolio.feature.manage.storage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Storage used: ${state.usedBytes / 1024} KB")
        Row {
            Text("Auto-cleanup trash after 30 days", modifier = Modifier.padding(8.dp))
            Switch(checked = state.autoCleanup, onCheckedChange = viewModel::setAutoCleanup)
        }
        Text("Recently deleted (${state.trash.size})")
        state.trash.forEach { photo ->
            Row {
                Text(photo.id, modifier = Modifier.padding(8.dp))
                Button(onClick = { viewModel.restore(photo.id) }) { Text("Restore") }
            }
        }
        if (state.trash.isNotEmpty()) {
            Button(onClick = viewModel::emptyTrash) { Text("Empty trash") }
        }
    }
}
