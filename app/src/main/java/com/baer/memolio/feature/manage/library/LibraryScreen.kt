package com.baer.memolio.feature.manage.library

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.ProGate

@Composable
fun LibraryScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    LibraryContent(
        state = state,
        onCreateAlbum = viewModel::createAlbum,
        onOpenAlbum = viewModel::openAlbum,
        onToggleSelect = viewModel::toggleSelection,
        onFavorite = { viewModel.favoriteSelected(true) },
        onDelete = viewModel::deleteSelected,
        onOpenPaywall = onOpenPaywall,
        modifier = modifier
    )
}

@Composable
fun LibraryContent(
    state: LibraryUiState,
    onCreateAlbum: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newAlbumName by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        ProGate(feature = ProFeature.ALBUMS, isPro = state.isPro, onUpsell = onOpenPaywall) {
            OutlinedTextField(
                value = newAlbumName,
                onValueChange = { newAlbumName = it },
                label = { Text("New album name") }
            )
            Button(
                onClick = {
                    if (newAlbumName.isNotBlank()) {
                        onCreateAlbum(newAlbumName.trim())
                        newAlbumName = ""
                    }
                }
            ) { Text("Create album") }
        }

        if (state.openAlbumId == null) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.albums, key = { it.id }) { album ->
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(album.name, modifier = Modifier.padding(16.dp))
                        Button(onClick = { onOpenAlbum(album.id) }) { Text("Open") }
                    }
                }
            }
        } else {
            OutlinedButton(onClick = { onOpenAlbum("") }) { Text("Back to albums") }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.openAlbumPhotos, key = { it.id }) { photo ->
                    PhotoThumb(
                        photo = photo,
                        selected = photo.id in state.selectedIds,
                        onClick = { onToggleSelect(photo.id) }
                    )
                }
            }
            if (state.selectedIds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = onFavorite) { Text("Favorite") }
                    Button(onClick = onDelete) { Text("Delete") }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumb(
    photo: Photo,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) {
        Modifier.border(3.dp, MaterialTheme.colorScheme.primary)
    } else {
        Modifier
    }
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .then(border)
            .clickable(onClick = onClick)
            .testTag("photo_${photo.id}")
    ) {
        AsyncImage(
            model = photo.thumbPath,
            contentDescription = photo.caption ?: photo.id,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (selected) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
            )
        }
    }
}
