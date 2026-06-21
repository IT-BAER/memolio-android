package com.baer.memolio.feature.manage.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
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
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioTextField
import com.baer.memolio.core.ui.component.SectionHead
import com.baer.memolio.core.ui.component.BlurredFillPhoto

@Composable
fun LibraryScreen(
    onOpenPaywall: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    // This ViewModel is activity-scoped, so it survives leaving and re-entering the Library
    // section. Reset to the album list on entry; otherwise re-entering would reopen whatever
    // album/preview the user last left.
    LaunchedEffect(Unit) { viewModel.resetView() }
    LibraryContent(
        state = state,
        onCreateAlbum = viewModel::createAlbum,
        onOpenAlbum = viewModel::openAlbum,
        onOpenAllPhotos = viewModel::openAllPhotos,
        onCloseAlbum = viewModel::closeAlbum,
        onDeleteAlbum = viewModel::deleteOpenAlbum,
        onOpenPreview = viewModel::openPreview,
        onClosePreview = viewModel::closePreview,
        onPreviewPageChanged = viewModel::openPreview,
        onToggleHidden = viewModel::setInPlaylist,
        onToggleFavorite = viewModel::favorite,
        onDeletePhoto = viewModel::deletePhoto,
        onToggleSelect = viewModel::toggleSelection,
        onClearSelection = viewModel::clearSelection,
        onFavoriteSelected = { viewModel.favoriteSelected(true) },
        onToggleHiddenSelected = viewModel::toggleHiddenSelected,
        onDeleteSelected = viewModel::deleteSelected,
        onOpenPaywall = onOpenPaywall,
        modifier = modifier
    )
}

@Composable
fun LibraryContent(
    state: LibraryUiState,
    onCreateAlbum: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onCloseAlbum: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenAllPhotos: () -> Unit = {},
    onOpenPreview: (String) -> Unit = {},
    onClosePreview: () -> Unit = {},
    onPreviewPageChanged: (String) -> Unit = {},
    onToggleHidden: (String, Boolean) -> Unit = { _, _ -> },
    onToggleFavorite: (String, Boolean) -> Unit = { _, _ -> },
    onDeletePhoto: (String) -> Unit = {},
    onDeleteAlbum: () -> Unit = {},
    onToggleSelect: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onFavoriteSelected: () -> Unit = {},
    onToggleHiddenSelected: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val albumOpen = !state.openAlbumId.isNullOrBlank()
    val preview = state.previewPhoto
    // System-back returns one level at a time: preview → clear selection → grid → album list →
    // (exit Manage). Each handler is registered after the one it should outrank (last enabled
    // handler wins); selection and preview are mutually exclusive.
    BackHandler(enabled = albumOpen) { onCloseAlbum() }
    BackHandler(enabled = state.selectionMode) { onClearSelection() }
    BackHandler(enabled = preview != null) { onClosePreview() }
    when {
        preview != null -> PhotoPreview(
            photos = state.openAlbumPhotos,
            initialPhotoId = state.previewPhotoId,
            onClose = onClosePreview,
            onPageChanged = onPreviewPageChanged,
            onToggleHidden = onToggleHidden,
            onToggleFavorite = onToggleFavorite,
            onDelete = onDeletePhoto,
            modifier = modifier
        )
        albumOpen -> AlbumDetailView(
            state = state,
            onCloseAlbum = onCloseAlbum,
            onOpenPreview = onOpenPreview,
            onDeleteAlbum = onDeleteAlbum,
            onToggleSelect = onToggleSelect,
            onClearSelection = onClearSelection,
            onFavoriteSelected = onFavoriteSelected,
            onToggleHiddenSelected = onToggleHiddenSelected,
            onDeleteSelected = onDeleteSelected,
            modifier = modifier
        )
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
            // The "All photos" album (the default upload bucket) always leads the grid and
            // shows the whole live pool. It is filtered out of the list below so it is never
            // drawn twice.
            item(key = ALL_PHOTOS_ID) {
                AlbumCard(
                    name = stringResource(R.string.library_all_photos),
                    cover = state.allPhotos.firstOrNull()?.thumbPath,
                    count = state.allPhotos.size,
                    onClick = onOpenAllPhotos
                )
            }
            items(state.albums.filter { it.id != ALL_PHOTOS_ID }, key = { it.id }) { album ->
                AlbumCard(
                    name = album.name,
                    cover = state.albumCovers[album.id],
                    count = null,
                    onClick = { onOpenAlbum(album.id) }
                )
            }
        }
    }
}

/** Album tile: cover image with a bottom gradient, name (and optional count) overlaid. */
@Composable
private fun AlbumCard(name: String, cover: String?, count: Int?, onClick: () -> Unit) {
    MemolioCard(
        variant = CardVariant.Surface,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        interactive = true,
        onClick = onClick
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
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
            Column(Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 12.dp)) {
                Text(name, color = MemolioColors.TextPrimary, style = MemolioType.bodyLg)
                if (count != null) {
                    Text(
                        pluralStringResource(R.plurals.album_photos, count, count),
                        color = MemolioColors.TextSecondary,
                        style = MemolioType.sm
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumDetailView(
    state: LibraryUiState,
    onCloseAlbum: () -> Unit,
    onOpenPreview: (String) -> Unit,
    onDeleteAlbum: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onClearSelection: () -> Unit,
    onFavoriteSelected: () -> Unit,
    onToggleHiddenSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier
) {
    val title = when {
        state.isAllPhotosOpen -> stringResource(R.string.library_all_photos)
        else -> state.albums.firstOrNull { it.id == state.openAlbumId }?.name
            ?: stringResource(R.string.library_album_fallback)
    }
    val selecting = state.selectionMode
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete) {
        DeleteAlbumDialog(
            albumName = title,
            onConfirm = { confirmDelete = false; onDeleteAlbum() },
            onDismiss = { confirmDelete = false }
        )
    }
    Column(modifier.fillMaxSize()) {
        SectionHead(
            title = if (selecting)
                pluralStringResource(R.plurals.selected_count, state.selectedIds.size, state.selectedIds.size)
            else title,
            sub = if (selecting) null
            else pluralStringResource(R.plurals.album_photos, state.openAlbumPhotos.size, state.openAlbumPhotos.size),
            action = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (selecting) {
                        // Selection mode: the header back-action just clears the selection.
                        MemolioButton(
                            text = stringResource(R.string.action_done),
                            onClick = onClearSelection,
                            variant = ButtonVariant.Ghost,
                            size = ButtonSize.Sm,
                            icon = "check"
                        )
                    } else {
                        // The All-photos bucket is the default home for uncategorized photos
                        // and cannot be deleted; only user-created albums show the delete action.
                        if (!state.isAllPhotosOpen) {
                            MemolioButton(
                                text = stringResource(R.string.library_delete_album),
                                onClick = { confirmDelete = true },
                                variant = ButtonVariant.Ghost,
                                size = ButtonSize.Sm,
                                icon = "delete"
                            )
                        }
                        MemolioButton(
                            text = stringResource(R.string.library_albums),
                            onClick = onCloseAlbum,
                            variant = ButtonVariant.Ghost,
                            size = ButtonSize.Sm,
                            icon = "arrow_back"
                        )
                    }
                }
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
                    selectionMode = selecting,
                    // Tap previews; in selection mode it toggles instead. Long-press always
                    // toggles (and starts selection mode from the grid).
                    onClick = { if (selecting) onToggleSelect(photo.id) else onOpenPreview(photo.id) },
                    onLongClick = { onToggleSelect(photo.id) }
                )
            }
        }
        if (selecting) {
            val allSelectedHidden = state.openAlbumPhotos
                .filter { it.id in state.selectedIds }
                .let { sel -> sel.isNotEmpty() && sel.all { !it.inPlaylist } }
            SelectionActionBar(
                allHidden = allSelectedHidden,
                onFavorite = onFavoriteSelected,
                onToggleHidden = onToggleHiddenSelected,
                onDelete = onDeleteSelected
            )
        }
    }
}

/** Bulk actions for the long-press selection: favorite, hide/show, delete the checked photos. */
@Composable
private fun SelectionActionBar(allHidden: Boolean, onFavorite: () -> Unit, onToggleHidden: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemolioButton(stringResource(R.string.library_favorite), onFavorite, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "favorite")
        MemolioButton(stringResource(if (allHidden) R.string.library_show else R.string.library_hide), onToggleHidden, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "playlist_play")
        MemolioButton(stringResource(R.string.library_delete), onDelete, variant = ButtonVariant.Ghost, size = ButtonSize.Sm, icon = "delete")
    }
}

/** Confirms deleting a user album; its photos are kept and moved to the All-photos bucket. */
@Composable
private fun DeleteAlbumDialog(albumName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MemolioColors.SurfaceCard,
        title = { Text(stringResource(R.string.library_delete_album_title), color = MemolioColors.TextPrimary, style = MemolioType.h3) },
        text = {
            Text(
                stringResource(R.string.library_delete_album_body, albumName),
                color = MemolioColors.TextSecondary,
                style = MemolioType.body
            )
        },
        confirmButton = {
            MemolioButton(
                text = stringResource(R.string.library_delete),
                onClick = onConfirm,
                variant = ButtonVariant.Secondary,
                size = ButtonSize.Sm,
                icon = "delete"
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

/**
 * Grid thumbnail: tap opens the preview (or toggles selection in selection mode); long-press
 * toggles selection. Shows a favorite heart, a dimmed "Hidden" badge, and a check when selected.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoThumb(
    photo: Photo,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
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
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .testTag("photo_${photo.id}")
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
        if (selectionMode) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MemolioColors.Ink000.copy(alpha = 0.5f))
            ) {
                Symbol(
                    if (selected) "check_circle" else "add",
                    size = 22.sp,
                    tint = if (selected) MemolioColors.Teal else MemolioColors.Paper
                )
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
 * Full-screen swipeable photo preview. Each page renders the photo full-size over the same
 * blurred-fill "bokeh" backdrop the slideshow uses; horizontal swipe moves to the next/previous
 * photo. Per-photo actions (hide/show, favorite, delete) act on the current page. Works the
 * same in a real album and in "All photos".
 */
@Composable
private fun PhotoPreview(
    photos: List<Photo>,
    initialPhotoId: String?,
    onClose: () -> Unit,
    onPageChanged: (String) -> Unit,
    onToggleHidden: (String, Boolean) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier
) {
    if (photos.isEmpty()) return
    val startIndex = remember(initialPhotoId) {
        photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0)
    }
    val pager = rememberPagerState(initialPage = startIndex) { photos.size }
    // Keep the ViewModel's "previewed photo" in sync with the settled page so delete/close
    // operate on what the user is actually looking at.
    LaunchedEffect(pager.settledPage, photos) {
        photos.getOrNull(pager.settledPage)?.let { onPageChanged(it.id) }
    }
    val current = photos.getOrNull(pager.currentPage) ?: photos.first()

    Box(modifier.fillMaxSize().background(MemolioColors.Ink000).testTag("preview_${current.id}")) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)
        ) { page ->
            BlurredFillPhoto(
                model = photos[page].displayCachePath,
                contentDescription = photos[page].caption ?: photos[page].id,
                kenBurns = false
            )
        }
        MemolioIconButton(
            icon = "close",
            contentDescription = stringResource(R.string.action_close),
            onClick = onClose,
            variant = IconButtonVariant.Glass,
            size = IconButtonSize.Sm,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        )
        if (!current.caption.isNullOrBlank()) {
            Text(
                current.caption!!,
                color = MemolioColors.Paper,
                style = MemolioType.bodyLg,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 84.dp, start = 24.dp, end = 24.dp)
            )
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemolioButton(
                text = stringResource(if (current.inPlaylist) R.string.library_hide else R.string.library_show),
                onClick = { onToggleHidden(current.id, !current.inPlaylist) },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Sm,
                icon = "playlist_play"
            )
            MemolioButton(
                text = stringResource(R.string.library_favorite),
                onClick = { onToggleFavorite(current.id, !current.favorite) },
                variant = if (current.favorite) ButtonVariant.Secondary else ButtonVariant.Ghost,
                size = ButtonSize.Sm,
                icon = "favorite"
            )
            MemolioButton(
                text = stringResource(R.string.library_delete),
                onClick = { onDelete(current.id) },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Sm,
                icon = "delete"
            )
        }
    }
}
