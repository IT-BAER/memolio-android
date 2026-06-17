package com.baer.memolio.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.baer.memolio.core.media.Transcoder.Decoded
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Real [Transcoder] backed by Android BitmapFactory. Delegates all sizing decisions to
 * [ImageScaler] (unit-tested). Intentionally thin; verify decode on real hardware (HEIC
 * support varies by OEM).
 */
class BitmapTranscoder @Inject constructor() : Transcoder {

    override fun readBounds(source: File): Decoded? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, opts)
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null
        return Decoded(opts.outWidth, opts.outHeight)
    }

    override fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int): Decoded {
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
