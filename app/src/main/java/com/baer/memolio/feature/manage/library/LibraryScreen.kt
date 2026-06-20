package com.baer.memolio.feature.manage.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
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
        onCloseAlbum = viewModel::closeAlbum,
        onToggleSelect = viewModel::toggleSelection,
        onFavorite = { viewModel.favoriteSelected(true) },
        onDelete = viewModel::deleteSelected,
        onOpenPaywall = onOpenPaywall,
        onOpenAllPhotos = viewModel::openAllPhotos,
        onCloseAllPhotos = viewModel::closeAllPhotos,
        onToggleInPlaylist = viewModel::setInPlaylist,
        modifier = modifier
    )
}

@Composable
fun LibraryContent(
    state: LibraryUiState,
    onCreateAlbum: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onCloseAlbum: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenAllPhotos: () -> Unit = {},
    onCloseAllPhotos: () -> Unit = {},
    onToggleInPlaylist: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val albumOpen = !state.openAlbumId.isNullOrBlank()
    // While a sub-view is open, the system back button returns to the albums list rather
    // than bubbling up to exit Manage (these handlers outrank ManageScaffold's because they
    // are composed later in the detail pane). All-photos and album-open are exclusive.
    BackHandler(enabled = state.showAllPhotos) { onCloseAllPhotos() }
    BackHandler(enabled = albumOpen) { onCloseAlbum() }
    when {
        state.showAllPhotos -> AllPhotosView(state, onCloseAllPhotos, onToggleInPlaylist, modifier)
        albumOpen -> AlbumDetailView(state, onCloseAlbum, onToggleSelect, onFavorite, onDelete, modifier)
        else -> AlbumsView(state, onCreateAlbum, onOpenAlbum, onOpenAllPhotos, onOpenPaywall, modifier)
    }
}

@Composable
private fun AlbumsView(
    state: LibraryUiState,
    onCreateAlbum: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenAllPhotos: () -> Unit,
    onOpenPaywall: () -> Unit,
    modifier: Modifier
) {
    var newAlbumName by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize()) {
        SectionHead(title = stringResource(R.string.library_title), sub = stringResource(R.string.library_subtitle))
        // Free "All photos" entry — curate the whole pool (hide/show photos from the slideshow).
        MemolioCard(
            variant = CardVariant.Surface,
            interactive = true,
            onClick = onOpenAllPhotos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Symbol("photo_library", size = 26.sp, tint = MemolioColors.Teal)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.library_all_photos), color = MemolioColors.TextPrimary, style = MemolioType.bodyLg)
                    Text(
                        pluralStringResource(R.plurals.album_photos, state.allPhotos.size, state.allPhotos.size),
                        color = MemolioColors.TextSecondary,
                        style = MemolioType.sm
                    )
                }
                Symbol("chevron_right", size = 24.sp, tint = MemolioColors.TextTertiary)
            }
        }
        Spacer(Modifier.height(20.dp))
        ProGate(feature = ProFeature.ALBUMS, isPro = state.isPro, onUpsell = onOpenPaywall) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                MemolioTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    label = stringResource(R.string.library_new_album_label),
                    modifier = Modifier.widthIn(max = 320.dp).weight(1f, fill = false)
                )
                MemolioButton(
                    text = stringResource(R.string.library_create_album),
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
        Spacer(Modifier.height(24.dp))
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
                        val cover = state.albumCovers[album.id]
                        if (!cover.isNullOrBlank()) {
                            AsyncImage(
                                model = cover,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(MemolioColors.Ink150))
                        }
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
    onCloseAlbum: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier
) {
    val title = state.albums.firstOrNull { it.id == state.openAlbumId }?.name ?: stringResource(R.string.library_album_fallback)
    Column(modifier.fillMaxSize()) {
        SectionHead(
            title = title,
            sub = pluralStringResource(R.plurals.album_photos, state.openAlbumPhotos.size, state.openAlbumPhotos.size),
            action = {
                MemolioButton(
                    text = stringResource(R.string.library_albums),
                    onClick = onCloseAlbum,
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
                    pluralStringResource(R.plurals.selected_count, state.selectedIds.size, state.selectedIds.size),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.sm
                )
                MemolioButton(stringResource(R.string.library_favorite), onFavorite, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "favorite")
                MemolioButton(stringResource(R.string.library_delete), onDelete, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "delete")
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

/**
 * Free "All photos" pool view: the whole live pool with per-photo slideshow toggles. Tapping
 * a photo hides/shows it from the slideshow (it stays in the library either way).
 */
@Composable
private fun AllPhotosView(
    state: LibraryUiState,
    onClose: () -> Unit,
    onToggleInPlaylist: (String, Boolean) -> Unit,
    modifier: Modifier
) {
    Column(modifier.fillMaxSize()) {
        SectionHead(
            title = stringResource(R.string.library_all_photos),
            sub = pluralStringResource(R.plurals.album_photos, state.allPhotos.size, state.allPhotos.size),
            action = {
                MemolioButton(
                    text = stringResource(R.string.library_albums),
                    onClick = onClose,
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Sm,
                    icon = "arrow_back"
                )
            }
        )
        Text(
            stringResource(R.string.library_pool_hint),
            color = MemolioColors.TextSecondary,
            style = MemolioType.sm,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(state.allPhotos, key = { it.id }) { photo ->
                PoolPhotoThumb(
                    photo = photo,
                    onToggle = { onToggleInPlaylist(photo.id, !photo.inPlaylist) }
                )
            }
        }
    }
}

@Composable
private fun PoolPhotoThumb(photo: Photo, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .aspectRatio(4f / 3f)
            .clip(shape)
            .border(1.dp, MemolioColors.BorderSoft, shape)
            .clickable(onClick = onToggle)
            .testTag("pool_${photo.id}")
    ) {
        AsyncImage(
            model = photo.thumbPath,
            contentDescription = photo.caption ?: photo.id,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(if (photo.inPlaylist) 1f else 0.32f)
        )
        if (!photo.inPlaylist) {
            Box(
                Modifier.align(Alignment.Center)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MemolioColors.Ink000.copy(alpha = 0.72f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(stringResource(R.string.photo_hidden), color = MemolioColors.Paper, style = MemolioType.sm)
            }
        }
    }
}
