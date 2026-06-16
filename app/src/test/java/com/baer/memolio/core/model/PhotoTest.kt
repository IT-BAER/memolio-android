package com.baer.memolio.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PhotoTest {
    @Test
    fun photoWithNullDeletedAtIsLive() {
        val photo = samplePhoto(deletedAt = null)
        assertThat(photo.isInTrash).isFalse()
    }

    @Test
    fun photoWithDeletedAtIsInTrash() {
        val photo = samplePhoto(deletedAt = 1_000L)
        assertThat(photo.isInTrash).isTrue()
    }

    private fun samplePhoto(deletedAt: Long?) = Photo(
        id = "p1",
        originalPath = "/photos/p1.jpg",
        displayCachePath = "/cache/display/p1.jpg",
        thumbPath = "/cache/thumb/p1.jpg",
        contentHash = "abc",
        width = 4000,
        height = 3000,
        orientation = 0,
        caption = null,
        albumId = "a1",
        favorite = false,
        sortOrder = 0,
        addedAt = 0L,
        sourceDevice = null,
        deletedAt = deletedAt
    )
}
