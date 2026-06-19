package com.baer.memolio.feature.manage.wallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
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
fun WallpaperScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: WallpaperViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Wallpaper")
        state.available.forEach { id ->
            Row {
                RadioButton(selected = id == state.selectedId, onClick = { viewModel.select(id) })
                Text(id, modifier = Modifier.padding(8.dp))
            }
        }
        ProGate(
            feature = ProFeature.CUSTOM_WALLPAPER,
            isPro = state.isPro,
            onUpsell = onOpenPaywall
        ) {
            Text("Custom wallpapers unlocked.")
        }
    }
}
