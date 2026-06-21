package com.baer.memolio.core.data

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.baer.memolio.core.storage.FileStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun makeRepo(): WallpaperRepositoryImpl {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val storage = FileStorage(tmp.root)
        return WallpaperRepositoryImpl(context, storage, UnconfinedTestDispatcher())
    }

    @Test
    fun importCustomCopiesBytesAndReturnsCustomId() = runTest {
        val repo = makeRepo()
        val srcFile = tmp.newFile("source.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val uri = Uri.fromFile(srcFile)

        val id = repo.importCustom(uri)

        assertThat(id).isEqualTo(CUSTOM_WALLPAPER_FILE_ID)
        val path = repo.customWallpaperPath()
        assertThat(path).isNotNull()
        assertThat(java.io.File(path!!).readBytes()).isEqualTo(byteArrayOf(1, 2, 3, 4))
    }

    @Test
    fun customWallpaperPathIsNullBeforeImport() {
        val repo = makeRepo()
        assertThat(repo.customWallpaperPath()).isNull()
    }

    @Test
    fun clearCustomMakesPathNull() = runTest {
        val repo = makeRepo()
        val srcFile = tmp.newFile("source2.jpg").apply { writeBytes(byteArrayOf(5, 6)) }
        repo.importCustom(Uri.fromFile(srcFile))
        assertThat(repo.customWallpaperPath()).isNotNull()

        repo.clearCustom()

        assertThat(repo.customWallpaperPath()).isNull()
    }
}
