package com.baer.memolio.feature.manage.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.ui.ProGate

@Composable
fun PlaylistScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: PlaylistViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        ProGate(feature = ProFeature.ALBUMS, isPro = state.isPro, onUpsell = onOpenPaywall) {
            Text("Active albums")
            state.allAlbums.forEach { album ->
                Row {
                    Switch(
                        checked = album.id in state.activeAlbumIds,
                        onCheckedChange = { viewModel.toggleAlbum(album.id) }
                    )
                    Text(album.name, modifier = Modifier.padding(8.dp))
                }
            }
        }
        Row {
            Text("Shuffle", modifier = Modifier.padding(8.dp))
            Switch(checked = state.shuffle, onCheckedChange = viewModel::setShuffle)
        }
        Text("Interval: ${state.intervalSeconds}s")
        Slider(
            value = state.intervalSeconds.toFloat(),
            onValueChange = { viewModel.setInterval(it.toInt()) },
            valueRange = 5f..300f
        )
        Row {
            Text("Clock", modifier = Modifier.padding(8.dp))
            Switch(checked = state.showClock, onCheckedChange = viewModel::setShowClock)
        }
        Row {
            Text("Date", modifier = Modifier.padding(8.dp))
            Switch(checked = state.showDate, onCheckedChange = viewModel::setShowDate)
        }
        Row {
            Text("Caption", modifier = Modifier.padding(8.dp))
            Switch(checked = state.showCaption, onCheckedChange = viewModel::setShowCaption)
        }
    }
}
