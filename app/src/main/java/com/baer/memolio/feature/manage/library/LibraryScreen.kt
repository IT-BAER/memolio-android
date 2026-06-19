package com.baer.memolio.feature.manage.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var newAlbumName by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = newAlbumName,
            onValueChange = { newAlbumName = it },
            label = { Text("New album name") }
        )
        Button(
            onClick = {
                if (newAlbumName.isNotBlank()) {
                    viewModel.createAlbum(newAlbumName.trim())
                    newAlbumName = ""
                }
            }
        ) { Text("Create album") }

        if (state.openAlbumId == null) {
            LazyVerticalGrid(columns = GridCells.Adaptive(160.dp), modifier = Modifier.fillMaxSize()) {
                items(state.albums, key = { it.id }) { album ->
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(album.name, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.openAlbum(album.id) }) { Text("Open") }
                    }
                }
            }
        } else {
            Button(onClick = { viewModel.openAlbum("") }) { Text("Back to albums") }
            LazyVerticalGrid(columns = GridCells.Adaptive(120.dp), modifier = Modifier.fillMaxSize()) {
                items(state.openAlbumPhotos, key = { it.id }) { photo ->
                    Card(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = if (photo.id in state.selectedIds) "[x] ${photo.id}" else photo.id,
                            modifier = Modifier.padding(8.dp)
                        )
                        Button(onClick = { viewModel.toggleSelection(photo.id) }) { Text("Select") }
                    }
                }
            }
            if (state.selectedIds.isNotEmpty()) {
                Button(onClick = { viewModel.favoriteSelected(true) }) { Text("Favorite") }
                Button(onClick = { viewModel.deleteSelected() }) { Text("Delete") }
            }
        }
    }
}
