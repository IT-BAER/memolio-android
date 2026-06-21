package com.baer.memolio.core.ui.component

import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FocalAlignmentTest {

    @Test
    fun `null focal returns Alignment Center`() {
        assertThat(focalAlignment(null, null)).isEqualTo(Alignment.Center)
    }

    @Test
    fun `partial focal x only returns Alignment Center`() {
        assertThat(focalAlignment(0.3f, null)).isEqualTo(Alignment.Center)
    }

    @Test
    fun `focal 0 0 returns top-left BiasAlignment`() {
        assertThat(focalAlignment(0f, 0f)).isEqualTo(BiasAlignment(-1f, -1f))
    }

    @Test
    fun `focal 0_5 0_5 returns center BiasAlignment`() {
        assertThat(focalAlignment(0.5f, 0.5f)).isEqualTo(BiasAlignment(0f, 0f))
    }

    @Test
    fun `focal 1 1 returns bottom-right BiasAlignment`() {
        assertThat(focalAlignment(1f, 1f)).isEqualTo(BiasAlignment(1f, 1f))
    }
}
