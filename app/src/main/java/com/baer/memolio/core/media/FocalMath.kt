package com.baer.memolio.core.media

/** A detected face box in source-image pixel coords. */
data class FaceBox(val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * Union of all face boxes, center of that union, normalized to 0..1 by image size.
 * Returns null if [faces] is empty or image dims are non-positive.
 * Result is clamped to [0f, 1f].
 */
fun computeFocalPoint(faces: List<FaceBox>, imageWidth: Int, imageHeight: Int): FocalPoint? {
    if (faces.isEmpty() || imageWidth <= 0 || imageHeight <= 0) return null

    val unionLeft = faces.minOf { it.left }
    val unionTop = faces.minOf { it.top }
    val unionRight = faces.maxOf { it.right }
    val unionBottom = faces.maxOf { it.bottom }

    val centerX = (unionLeft + unionRight) / 2f
    val centerY = (unionTop + unionBottom) / 2f

    val normalizedX = (centerX / imageWidth).coerceIn(0f, 1f)
    val normalizedY = (centerY / imageHeight).coerceIn(0f, 1f)

    return FocalPoint(normalizedX, normalizedY)
}
