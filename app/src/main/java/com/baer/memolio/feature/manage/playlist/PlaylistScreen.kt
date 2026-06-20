package com.baer.memolio.feature.manage.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.ProGate
import com.baer.memolio.core.ui.component.AdaptiveTwoPane
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioSlider
import com.baer.memolio.core.ui.component.MemolioSwitch
import com.baer.memolio.core.ui.component.SectionHead

@Composable
fun PlaylistScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: PlaylistViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier.verticalScroll(rememberScrollState())) {
        SectionHead(title = stringResource(R.string.playlist_title), sub = stringResource(R.string.playlist_subtitle))
        AdaptiveTwoPane(
            // Active albums
            first = { paneModifier ->
                MemolioCard(modifier = paneModifier, variant = CardVariant.Surface) {
                    Column {
                        Eyebrow(stringResource(R.string.playlist_active_albums))
                        ProGate(feature = ProFeature.ALBUMS, isPro = state.isPro, onUpsell = onOpenPaywall) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                state.allAlbums.forEach { album ->
                                    MemolioSwitch(
                                        checked = album.id in state.activeAlbumIds,
                                        onCheckedChange = { viewModel.toggleAlbum(album.id) },
                                        label = album.name
                                    )
                                }
                            }
                        }
                    }
                }
            },
            // Display
            second = { paneModifier ->
                MemolioCard(modifier = paneModifier, variant = CardVariant.Surface) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Eyebrow(stringResource(R.string.playlist_display))
                        MemolioSlider(
                            value = state.intervalSeconds,
                            onValueChange = viewModel::setInterval,
                            valueRange = 5f..300f,
                            suffix = stringResource(R.string.playlist_interval_suffix),
                            label = stringResource(R.string.playlist_interval)
                        )
                        MemolioSwitch(state.shuffle, viewModel::setShuffle, label = stringResource(R.string.playlist_shuffle))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(MemolioColors.BorderSoft))
                        MemolioSwitch(state.showClock, viewModel::setShowClock, label = stringResource(R.string.playlist_show_clock))
                        MemolioSwitch(state.showDate, viewModel::setShowDate, label = stringResource(R.string.playlist_show_date))
                        MemolioSwitch(state.showCaption, viewModel::setShowCaption, label = stringResource(R.string.playlist_show_caption))
                    }
                }
            }
        )
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = MemolioColors.TextTertiary,
        style = MemolioType.label,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}
