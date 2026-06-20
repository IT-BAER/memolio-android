package com.baer.memolio.feature.manage.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
 * no ViewModel. Guards the layout regression where the photo grid's fillMaxSize pushed
 * the Favorite/Delete actions off-screen and made them unreachable.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
@Config(sdk = [34])
class LibraryContentTest {

    @get:Rule val composeRule = createComposeRule()

    private fun photo(id: String) = Photo(
        id = id, originalPath = "/o/$id.jpg", displayCachePath = "/d/$id.jpg",
        thumbPath = "/t/$id.jpg", contentHash = id, width = 100, height = 100,
        orientation = 0, caption = null, albumId = "all", favorite = false,
        sortOrder = 0, addedAt = 0L, sourceDevice = null, deletedAt = null
    )

    private fun content(state: LibraryUiState) {
        composeRule.setContent {
            MemolioTheme {
                LibraryContent(
                    state = state,
                    onCreateAlbum = {}, onOpenAlbum = {}, onToggleSelect = {},
                    onFavorite = {}, onDelete = {}, onOpenPaywall = {}
                )
            }
        }
    }

    @Test
    fun deleteActionIsReachableWhenPhotoSelected() {
        content(
            LibraryUiState(
                openAlbumId = "all",
                openAlbumPhotos = listOf(photo("p1"), photo("p2")),
                selectedIds = setOf("p1"),
                isPro = false
            )
        )
        composeRule.onNodeWithText("Delete").assertIsDisplayed()
        composeRule.onNodeWithText("Favorite").assertIsDisplayed()
    }

    @Test
    fun rendersPhotoThumbnailNodes() {
        content(
            LibraryUiState(
                openAlbumId = "all",
                openAlbumPhotos = listOf(photo("p1")),
                isPro = false
            )
        )
        composeRule.onNodeWithTag("photo_p1").assertIsDisplayed()
    }

    @Test
    fun noActionsWhenNothingSelected() {
        content(
            LibraryUiState(
                openAlbumId = "all",
                openAlbumPhotos = listOf(photo("p1")),
                selectedIds = emptySet(),
                isPro = false
            )
        )
        composeRule.onNodeWithText("Delete").assertDoesNotExist()
    }
}
