package com.baer.memolio.core.media

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImageScalerTest {
    @Test
    fun targetSizeKeepsAspectAndFitsWithinMaxEdge() {
        // 4000x3000 into max edge 2048 -> 2048x1536
        val (w, h) = ImageScaler.targetSize(4000, 3000, maxEdge = 2048)
        assertThat(w).isEqualTo(2048)
        assertThat(h).isEqualTo(1536)
    }

    @Test
    fun targetSizePortrait() {
        val (w, h) = ImageScaler.targetSize(3000, 4000, maxEdge = 2048)
        assertThat(w).isEqualTo(1536)
        assertThat(h).isEqualTo(2048)
    }

    @Test
    fun smallerThanMaxIsUnchanged() {
        val (w, h) = ImageScaler.targetSize(800, 600, maxEdge = 2048)
        assertThat(w).isEqualTo(800)
        assertThat(h).isEqualTo(600)
    }

    @Test
    fun inSampleSizeIsLargestPowerOfTwoNotExceedingRatio() {
        // 4000x3000 reqW=1000 reqH=1000 -> ratio ~3 -> inSampleSize 2
        assertThat(ImageScaler.inSampleSize(4000, 3000, 1000, 1000)).isEqualTo(2)
        assertThat(ImageScaler.inSampleSize(4000, 3000, 250, 250)).isEqualTo(8)
        assertThat(ImageScaler.inSampleSize(800, 600, 2048, 2048)).isEqualTo(1)
    }
}
