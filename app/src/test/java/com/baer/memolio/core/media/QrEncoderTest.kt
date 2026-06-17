package com.baer.memolio.core.media

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrEncoderTest {

    private val encoder = QrEncoder()

    @Test
    fun encodesBitmapOfRequestedSize() {
        val bitmap = encoder.encode("http://192.168.1.42:8080/?t=abc", sizePx = 256)
        assertThat(bitmap).isNotNull()
        assertThat(bitmap.width).isEqualTo(256)
        assertThat(bitmap.height).isEqualTo(256)
    }

    @Test
    fun producesBlackAndWhitePixels() {
        val bitmap = encoder.encode("hello-memolio", sizePx = 128)
        val pixels = IntArray(128 * 128)
        bitmap.getPixels(pixels, 0, 128, 0, 0, 128, 128)
        val distinct = pixels.toSet()
        // A QR has both modules and quiet zone -> at least black + white present.
        assertThat(distinct).contains(Color.BLACK)
        assertThat(distinct).contains(Color.WHITE)
    }
}
