package com.baer.memolio.core.storage

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileStorageTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun pathsAreNestedUnderRoot() {
        val storage = FileStorage(tmp.root)
        assertThat(storage.originalFile("p1", "jpg").path)
            .isEqualTo(tmp.root.resolve("photos/p1.jpg").path)
        assertThat(storage.displayCacheFile("p1").path)
            .isEqualTo(tmp.root.resolve("cache/display/p1.jpg").path)
        assertThat(storage.thumbFile("p1").path)
            .isEqualTo(tmp.root.resolve("cache/thumb/p1.jpg").path)
    }

    @Test
    fun writeOriginalCreatesFileAndReturnsIt() {
        val storage = FileStorage(tmp.root)
        val out = storage.writeOriginal("p1", "jpg") { it.write(byteArrayOf(1, 2, 3)) }
        assertThat(out.exists()).isTrue()
        assertThat(out.readBytes()).isEqualTo(byteArrayOf(1, 2, 3))
    }

    @Test
    fun deleteAllRemovesEveryDerivedFile() {
        val storage = FileStorage(tmp.root)
        storage.writeOriginal("p1", "jpg") { it.write(byteArrayOf(1)) }
        storage.displayCacheFile("p1").apply { parentFile?.mkdirs(); writeBytes(byteArrayOf(2)) }
        storage.thumbFile("p1").apply { parentFile?.mkdirs(); writeBytes(byteArrayOf(3)) }

        storage.deleteAll("p1", "jpg")

        assertThat(storage.originalFile("p1", "jpg").exists()).isFalse()
        assertThat(storage.displayCacheFile("p1").exists()).isFalse()
        assertThat(storage.thumbFile("p1").exists()).isFalse()
    }
}
