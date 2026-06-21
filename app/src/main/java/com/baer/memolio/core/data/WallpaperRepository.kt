package com.baer.memolio.core.data

import android.content.Context
import android.net.Uri
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.storage.FileStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface WallpaperRepository {
    /** Copies the picked image into app storage as the single custom wallpaper. Returns the id to persist ("custom"). */
    suspend fun importCustom(uri: Uri): String
    /** Absolute path to the stored custom wallpaper, or null if none imported. */
    fun customWallpaperPath(): String?
    /** Removes the custom wallpaper file. */
    suspend fun clearCustom()
}

class WallpaperRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileStorage: FileStorage,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WallpaperRepository {

    override suspend fun importCustom(uri: Uri): String = withContext(ioDispatcher) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open wallpaper uri: $uri" }
            fileStorage.writeCustomWallpaper { out -> input.copyTo(out) }
        }
        CUSTOM_WALLPAPER_FILE_ID
    }

    override fun customWallpaperPath(): String? =
        fileStorage.customWallpaperFile().takeIf { it.exists() }?.absolutePath

    override suspend fun clearCustom() = withContext(ioDispatcher) {
        fileStorage.deleteCustomWallpaper()
    }
}

/** The wallpaperId persisted in settings for the single custom slot. Mirrors core.ui.CUSTOM_WALLPAPER_ID. */
const val CUSTOM_WALLPAPER_FILE_ID = "custom"
