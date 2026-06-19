package com.baer.memolio.feature.manage

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ManageSectionTest {
    @Test
    fun defaultSectionIsLibrary() {
        assertThat(ManageSection.default).isEqualTo(ManageSection.Library)
    }

    @Test
    fun allSevenSectionsPresentInOrder() {
        assertThat(ManageSection.entries.map { it.name }).containsExactly(
            "Library", "Playlist", "AddPhotos", "Appliance", "Storage", "Wallpaper", "About"
        ).inOrder()
    }
}
