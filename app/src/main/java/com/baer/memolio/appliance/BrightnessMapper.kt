package com.baer.memolio.appliance

import kotlin.math.ln

/**
 * Pure lux -> screen-brightness [0f..1f] mapping. No Android dependencies.
 *
 * Perceived brightness is roughly logarithmic, so we map log(lux) linearly between
 * a [floor] (dark room) and 1.0 (at/above [maxLux]). Result is always clamped to
 * [floor, 1.0]. Maps to WindowManager.LayoutParams.screenBrightness at the call site.
 */
object BrightnessMapper {

    fun luxToBrightness(lux: Float, floor: Float = 0.05f, maxLux: Float = 2000f): Float {
        val clampedLux = lux.coerceIn(0f, maxLux)
        if (clampedLux <= 0f) return floor
        val t = (ln(clampedLux + 1f) / ln(maxLux + 1f)).coerceIn(0f, 1f)
        return (floor + t * (1f - floor)).coerceIn(floor, 1f)
    }
}
