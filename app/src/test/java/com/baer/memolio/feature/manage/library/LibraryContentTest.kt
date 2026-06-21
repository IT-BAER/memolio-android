package com.baer.memolio.feature.manage.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import com.google.common.truth.Truth.assertThat
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.ui.MemolioTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Behavior tests for [LibraryContent]. Feed fixed [LibraryUiState] instances directly,
 * no ViewModel. Covers the album list "All photos" tile, tap-to-preview, the preview's
 * per-photo actions, the hidden badge, and album back-navigation.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class LibraryContentTest {

    @get:Rule val composeRule = createComposeRule()

    private fun photo(id: String, inPlaylist: Boolean = true) = Photo(
        id = id, originalPath = "/o/$id.jpg", displayCachePath = "/d/$id.jpg",
        thumbPath = "/t/$id.jpg", contentHash = id, width = 100, height = 100,
        orientation = 0, caption = null, albumId = "a1", favorite = false,
        sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = null, inPlaylist = inPlaylist
    )

    private fun content(
        state: LibraryUiState,
        onCloseAlbum: () -> Unit = {},
        onOpenPreview: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            MemolioTheme {
                LibraryContent(
                    state = state,
                    onCreateAlbum = {}, onOpenAlbum = {}, onCloseAlbum = onCloseAlbum,
                    onOpenPaywall = {}, onOpenPreview = onOpenPreview
                )
            }
        }
    }

    @Test
    fun albumListShowsAllPhotosTile() {
        content(LibraryUiState(allPhotos = listOf(photo("p1"), photo("p2")), isPro = false))
        composeRule.onNodeWithText("All photos").assertIsDisplayed()
    }

    @Test
    fun tappingThumbnailOpensPreview() {
        var opened: String? = null
        content(
            LibraryUiState(openAlbumId = "a1", openAlbumPhotos = listOf(photo("p1"))),
            onOpenPreview = { opened = it }
        )
        composeRule.onNodeWithTag("photo_p1").performClick()
        assertThat(opened).isEqualTo("p1")
    }

    @Test
    fun previewShowsPerPhotoActions() {
        content(
            LibraryUiState(
                openAlbumId = "a1",
                openAlbumPhotos = listOf(photo("p1")),
                previewPhotoId = "p1"
            )
        )
        composeRule.onNodeWithTag("preview_p1").assertIsDisplayed()
        composeRule.onNodeWithText("Hide").assertIsDisplayed()
        composeRule.onNodeWithText("Favorite").assertIsDisplayed()
        composeRule.onNodeWithText("Delete").assertIsDisplayed()
    }

    @Test
    fun hiddenPhotoShowsBadgeInGrid() {
        content(
            LibraryUiState(
                openAlbumId = "a1",
                openAlbumPhotos = listOf(photo("p1", inPlaylist = false))
            )
        )
        composeRule.onNodeWithText("Hidden").assertIsDisplayed()
    }

    @Test
    fun realAlbumShowsDeleteAlbumAction() {
        content(
            LibraryUiState(
                albums = listOf(Album("fam", "Family", null, 0L, 1)),
                openAlbumId = "fam",
                openAlbumPhotos = listOf(photo("p1"))
            )
        )
        composeRule.onNodeWithText("Delete album").assertIsDisplayed()
    }

    @Test
    fun allPhotosAlbumHidesDeleteAlbumAction() {
        content(
            LibraryUiState(
                openAlbumId = ALL_PHOTOS_ID,
                openAlbumPhotos = listOf(photo("p1"))
            )
        )
        composeRule.onNodeWithText("Delete album").assertDoesNotExist()
    }

    @Test
    fun albumsBackButtonClosesAlbum() {
        var closed = false
        content(
            LibraryUiState(openAlbumId = "a1", openAlbumPhotos = listOf(photo("p1"))),
            onCloseAlbum = { closed = true }
        )
        composeRule.onNodeWithText("Albums").performClick()
        assertThat(closed).isTrue()
    }

    @Test
    fun selectionModeShowsCountAndBulkActions() {
        content(
            LibraryUiState(
                openAlbumId = "a1",
                openAlbumPhotos = listOf(photo("p1"), photo("p2")),
                selectedIds = setOf("p1")
            )
        )
        composeRule.onNodeWithText("1 selected").assertIsDisplayed()
        composeRule.onNodeWithText("Favorite").assertIsDisplayed()
        composeRule.onNodeWithText("Hide").assertIsDisplayed()
        composeRule.onNodeWithText("Delete").assertIsDisplayed()
    }

    @Test
    fun longPressThumbnailTogglesSelection() {
        var toggled: String? = null
        composeRule.setContent {
            MemolioTheme {
                LibraryContent(
                    state = LibraryUiState(openAlbumId = "a1", openAlbumPhotos = listOf(photo("p1"))),
                    onCreateAlbum = {}, onOpenAlbum = {}, onCloseAlbum = {}, onOpenPaywall = {},
                    onToggleSelect = { toggled = it }
                )
            }
        }
        composeRule.onNodeWithTag("photo_p1").performTouchInput { longClick() }
        assertThat(toggled).isEqualTo("p1")
    }
}
