package com.baer.memolio.feature.manage.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.model.Photo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlbumCreateFlowTest {

    @get:Rule val composeRule = createComposeRule()
    private val dispatcher = UnconfinedTestDispatcher()

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: android.app.Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

    private class FakeAlbumRepo : AlbumRepository {
        val albums = MutableStateFlow<List<Album>>(emptyList())
        override fun observeAlbums(): Flow<List<Album>> = albums
        override suspend fun upsert(album: Album) { albums.value = albums.value.filterNot { it.id == album.id } + album }
        override suspend fun delete(id: String) { albums.value = albums.value.filterNot { it.id == id } }
    }

    private class FakePhotoRepo : PhotoRepository {
        override fun observePhotos(albumId: String): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observeTrash(): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observeAllLivePhotos(): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observeSlideshowPool(): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override fun observeSlideshowInAlbums(albumIds: Set<String>): Flow<List<Photo>> = MutableStateFlow(emptyList())
        override suspend fun setInPlaylist(id: String, inPlaylist: Boolean) {}
        override suspend fun isDuplicate(contentHash: String) = false
        override suspend fun add(
            id: String, originalPath: String, displayCachePath: String, thumbPath: String,
            contentHash: String, width: Int, height: Int, orientation: Int,
            caption: String?, albumId: String, sourceDevice: String?, addedAt: Long
        ) {}
        override suspend fun softDelete(id: String, now: Long) {}
        override suspend fun restore(id: String) {}
        override suspend fun purgeTrashOlderThan(threshold: Long) = 0
        override suspend fun moveToAlbum(id: String, albumId: String) {}
        override suspend fun setFavorite(id: String, favorite: Boolean) {}
        override suspend fun setCaption(id: String, caption: String?) {}
        override suspend fun reorder(orderedIds: List<String>) {}
    }

    @Test
    fun typingNameAndTappingCreateAddsAlbumToTheGrid() {
        val albumRepo = FakeAlbumRepo()
        val vm = LibraryViewModel(albumRepo, FakePhotoRepo(), FakeEntitlement(true), dispatcher) { 42L }

        composeRule.setContent { LibraryScreen(viewModel = vm) }

        composeRule.onNodeWithText("New album name").performTextInput("Holiday")
        composeRule.onNodeWithText("Create album").performClick()
        composeRule.waitForIdle()

        assertThat(albumRepo.albums.value.map { it.name }).containsExactly("Holiday")
        composeRule.onNodeWithText("Holiday").assertIsDisplayed()
    }
}
