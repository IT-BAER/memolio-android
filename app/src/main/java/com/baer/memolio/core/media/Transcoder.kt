package com.baer.memolio.core.media

import java.io.File

/**
 * Decode/encode seam for the import pipeline. Implemented for real by [BitmapTranscoder]
 * (Android BitmapFactory) and faked in unit tests (Robolectric cannot reliably decode
 * real image bytes).
 */
interface Transcoder {

    data class Decoded(val width: Int, val height: Int)

    /** Reads only the bounds (no pixel decode). Returns null if [source] is not a decodable image. */
    fun readBounds(source: File): Decoded?

    /** Writes a downscaled JPEG of [source] into [dest], longest edge <= [maxEdge]. Returns dest dims. */
    fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int = 88): Decoded
}
