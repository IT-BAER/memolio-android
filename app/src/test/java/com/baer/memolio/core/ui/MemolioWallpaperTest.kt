package com.baer.memolio.core.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The wallpaper draw math is extracted into pure helpers so the resolution-
 * independence and the burn-in drift can be unit-tested without rendering.
 */
class MemolioWallpaperTest {

    @Test
    fun angleVectorIsResolutionScaled() {
        // 148deg gradient on a 1000x1000 canvas vs 2000x2000 must point the same way
        val small = gradientVector(148f, Size(1000f, 1000f))
        val large = gradientVector(148f, Size(2000f, 2000f))
        val sx = small.second - small.first
        val lx = large.second - large.first
        // direction (normalized) identical
        val sLen = kotlin.math.hypot(sx.x, sx.y)
        val lLen = kotlin.math.hypot(lx.x, lx.y)
        assertThat(sx.x / sLen).isWithin(1e-4f).of(lx.x / lLen)
        assertThat(sx.y / sLen).isWithin(1e-4f).of(lx.y / lLen)
        // magnitude scales with canvas
        assertThat(lLen).isWithin(1e-3f).of(sLen * 2f)
    }

    @Test
    fun driftStaysWithinBounds() {
        // Over a full day the idle drift offset never exceeds the configured radius.
        var maxMag = 0f
        for (minute in 0..1440) {
            val d = idleDriftOffset(minute / 1440f, radiusPx = 40f)
            maxMag = maxOf(maxMag, kotlin.math.hypot(d.x, d.y))
        }
        assertThat(maxMag).isAtMost(40f + 1e-3f)
    }

    @Test
    fun driftIsContinuousAtWrap() {
        val start = idleDriftOffset(0f, radiusPx = 40f)
        val end = idleDriftOffset(1f, radiusPx = 40f)
        assertThat(end.x).isWithin(1e-3f).of(start.x)
        assertThat(end.y).isWithin(1e-3f).of(start.y)
    }
}
