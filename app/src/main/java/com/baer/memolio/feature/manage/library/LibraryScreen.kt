package com.baer.memolio.feature.manage.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.ProGate
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.component.ButtonSize
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioTextField
import com.baer.memolio.core.ui.component.SectionHead

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
    if (state.openAlbumId == null) {
        AlbumsView(state, onCreateAlbum, onOpenAlbum, onOpenPaywall, modifier)
    } else {
        AlbumDetailView(state, onOpenAlbum, onToggleSelect, onFavorite, onDelete, modifier)
    }
}

@Composable
private fun AlbumsView(
    state: LibraryUiState,
    onCreateAlbum: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenPaywall: () -> Unit,
    modifier: Modifier
) {
    var newAlbumName by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize()) {
        SectionHead(title = "Library", sub = "Group photos into albums")
        ProGate(feature = ProFeature.ALBUMS, isPro = state.isPro, onUpsell = onOpenPaywall) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                MemolioTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    label = "New album name",
                    modifier = Modifier.widthIn(max = 320.dp).weight(1f, fill = false)
                )
                MemolioButton(
                    text = "Create album",
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            onCreateAlbum(newAlbumName.trim())
                            newAlbumName = ""
                        }
                    },
                    variant = ButtonVariant.Primary,
                    icon = "add"
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.albums, key = { it.id }) { album ->
                MemolioCard(
                    variant = CardVariant.Surface,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    interactive = true,
                    onClick = { onOpenAlbum(album.id) }
                ) {
                    Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
                        Box(Modifier.fillMaxSize().background(MemolioColors.Ink150))
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    0.4f to Color.Transparent,
                                    1f to MemolioColors.Ink000.copy(alpha = 0.85f)
                                )
                            )
                        )
                        Text(
                            album.name,
                            color = MemolioColors.TextPrimary,
                            style = MemolioType.bodyLg,
                            modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumDetailView(
    state: LibraryUiState,
    onOpenAlbum: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier
) {
    val title = state.albums.firstOrNull { it.id == state.openAlbumId }?.name ?: "Album"
    Column(modifier.fillMaxSize()) {
        SectionHead(
            title = title,
            sub = "${state.openAlbumPhotos.size} photos",
            action = {
                MemolioButton(
                    text = "Albums",
                    onClick = { onOpenAlbum("") },
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Sm,
                    icon = "arrow_back"
                )
            }
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${state.selectedIds.size} selected",
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.sm
                )
                MemolioButton("Favorite", onFavorite, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "favorite")
                MemolioButton("Delete", onDelete, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "delete")
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
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .aspectRatio(4f / 3f)
            .clip(shape)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MemolioColors.Teal else MemolioColors.BorderSoft,
                shape
            )
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
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .clip(RoundedCornerShape(percent = 50)).background(MemolioColors.Ink000)
            ) {
                Symbol("check_circle", size = 22.sp, tint = MemolioColors.Teal)
            }
        }
        if (photo.favorite) {
            Symbol(
                "favorite",
                size = 20.sp,
                tint = MemolioColors.Paper,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            )
        }
    }
}
