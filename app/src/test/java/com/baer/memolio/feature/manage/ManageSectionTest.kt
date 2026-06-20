package com.baer.memolio.feature.manage

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ManageSectionTest {
    @Test
    fun defaultSectionIsLibrary() {
        assertThat(ManageSection.default).isEqualTo(ManageSection.Library)
    }

    @Test
    fun allSectionsPresentInOrder() {
        assertThat(ManageSection.entries.map { it.name }).containsExactly(
            "Library", "Playlist", "AddPhotos", "Appliance", "Storage", "Settings"
        ).inOrder()
    }
}
