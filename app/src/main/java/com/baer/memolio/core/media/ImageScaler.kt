package com.baer.memolio.core.media

import kotlin.math.roundToInt

/** Pure scaling math. No Android dependencies. */
object ImageScaler {

    /** Scaled (width,height) so the longest edge <= maxEdge, preserving aspect. Never upscales. */
    fun targetSize(srcW: Int, srcH: Int, maxEdge: Int): Pair<Int, Int> {
        val longest = maxOf(srcW, srcH)
        if (longest <= maxEdge) return srcW to srcH
        val scale = maxEdge.toDouble() / longest
        return (srcW * scale).roundToInt() to (srcH * scale).roundToInt()
    }

    /** Largest power-of-two sample size keeping both dims >= requested. Mirrors BitmapFactory guidance. */
    fun inSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var sample = 1
        if (srcH > reqH || srcW > reqW) {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while ((halfH / sample) >= reqH && (halfW / sample) >= reqW) {
                sample *= 2
            }
        }
        return sample
    }
}
