package com.baer.memolio.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Decodes a source image and writes a downscaled JPEG copy. Delegates all sizing
 * decisions to [ImageScaler] (which is unit-tested). This wrapper is intentionally
 * thin; verify it with an instrumented test on real hardware (HEIC support varies).
 */
class BitmapTranscoder {

    data class Decoded(val width: Int, val height: Int)

    /** Reads only the bounds (no pixel decode). Returns null if not a decodable image. */
    fun readBounds(source: File): Decoded? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, opts)
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null
        return Decoded(opts.outWidth, opts.outHeight)
    }

    /** Writes a downscaled JPEG of [source] into [dest], longest edge <= [maxEdge]. Returns dest dims. */
    fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int = 88): Decoded {
        val bounds = readBounds(source) ?: error("Not a decodable image: ${source.name}")
        val (targetW, targetH) = ImageScaler.targetSize(bounds.width, bounds.height, maxEdge)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = ImageScaler.inSampleSize(bounds.width, bounds.height, targetW, targetH)
        }
        val decoded = BitmapFactory.decodeFile(source.absolutePath, opts)
            ?: error("Decode failed: ${source.name}")
        val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
        dest.parentFile?.mkdirs()
        FileOutputStream(dest).use { scaled.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        if (scaled != decoded) decoded.recycle()
        scaled.recycle()
        return Decoded(targetW, targetH)
    }
}
