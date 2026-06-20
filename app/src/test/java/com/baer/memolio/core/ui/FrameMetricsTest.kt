package com.baer.memolio.core.ui

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pure-logic tests for [frameMetrics] — the short-edge (vmin) clamp rules that drive the
 * one frame overlay composition across portrait (primary) and landscape (secondary).
 * No Robolectric needed.
 */
class FrameMetricsTest {

    @Test
    fun portraitTablet_isPortraitTrue_andSizesOffShortEdge() {
        val m = frameMetrics(maxWidth = 800.dp, maxHeight = 1280.dp)
        assertThat(m.isPortrait).isTrue()
        // vmin = 800: clock 26vmin = 208 (within 88..260), insetX 6vmin = 48.
        assertThat(m.clockSize.value).isWithin(0.5f).of(208f)
        assertThat(m.insetX.value).isWithin(0.5f).of(48f)
        assertThat(m.dateSize.value).isWithin(0.5f).of(27.2f)
    }

    @Test
    fun landscapeTablet_isPortraitFalse_sameShortEdgeSizes() {
        val m = frameMetrics(maxWidth = 1280.dp, maxHeight = 800.dp)
        assertThat(m.isPortrait).isFalse()
        // Short edge is still 800 → identical overlay sizing, only the scrim direction differs.
        assertThat(m.clockSize.value).isWithin(0.5f).of(208f)
        assertThat(m.insetX.value).isWithin(0.5f).of(48f)
    }

    @Test
    fun square_treatedAsPortrait() {
        // h >= w counts as portrait.
        assertThat(frameMetrics(600.dp, 600.dp).isPortrait).isTrue()
    }

    @Test
    fun smallShortEdge_clampsToMinimums() {
        val m = frameMetrics(maxWidth = 300.dp, maxHeight = 300.dp)
        assertThat(m.clockSize.value).isWithin(0.5f).of(88f)   // clamp(88, 26vmin=78, 260)
        assertThat(m.insetX.value).isWithin(0.5f).of(24f)      // clamp(24, 6vmin=18, 64)
        assertThat(m.dateSize.value).isWithin(0.5f).of(20f)    // clamp(20, 3.4vmin=10.2, 40)
        assertThat(m.insetBottom.value).isWithin(0.5f).of(48f) // clamp(48, 10vmin=30, 120)
    }

    @Test
    fun largeShortEdge_clampsToMaximums() {
        val m = frameMetrics(maxWidth = 1200.dp, maxHeight = 1600.dp)
        assertThat(m.clockSize.value).isWithin(0.5f).of(260f)  // clamp(88, 26vmin=312, 260)
        assertThat(m.insetX.value).isWithin(0.5f).of(64f)      // clamp(24, 6vmin=72, 64)
        assertThat(m.dateSize.value).isWithin(0.5f).of(40f)    // clamp(20, 3.4vmin=40.8, 40)
        assertThat(m.insetBottom.value).isWithin(0.5f).of(120f)// clamp(48, 10vmin=120, 120)
    }
}
