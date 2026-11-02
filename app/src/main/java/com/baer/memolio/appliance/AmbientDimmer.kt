package com.baer.memolio.appliance

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Light-sensor -> target screen-brightness [0f..1f]. PURE policy lives in
 * [resolveBrightness] (unit-tested). The [start]/[stop] SensorEventListener wiring is
 * device-only. Graceful no-op when there is no TYPE_LIGHT sensor and when ambient dimming
 * is disabled — in those cases the manual AppSettings.brightness wins.
 *
 * The resolved value is exposed as [targetBrightness]; MainActivity applies it to
 * WindowManager.LayoutParams.screenBrightness.
 */
class AmbientDimmer(
    context: Context,
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val hasSensor: Boolean get() = lightSensor != null

    @Volatile private var lastLux: Float? = null
    @Volatile private var dimmingEnabled: Boolean = true
    @Volatile private var manualBrightness: Float = 0.7f

    private val easer = BrightnessEaser(scope, dispatcher, initial = manualBrightness)
    val targetBrightness: StateFlow<Float> = easer.brightness

    /** Push current settings; recomputes immediately so toggles take effect at once. */
    fun configure(dimmingEnabled: Boolean, manualBrightness: Float) {
        this.dimmingEnabled = dimmingEnabled
        this.manualBrightness = manualBrightness
        recompute()
    }

    fun start() {
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        recompute()
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            lastLux = event.values.firstOrNull()
            // Sensor readings jitter; ease toward the target instead of jumping so the
            // screen never visibly flickers. Settings changes (configure) still jump.
            easer.easeTo(resolveBrightness(dimmingEnabled, hasSensor, lastLux, manualBrightness))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun recompute() {
        easer.jumpTo(
            resolveBrightness(
                dimmingEnabled = dimmingEnabled,
                hasSensor = hasSensor,
                lastLux = lastLux,
                manualBrightness = manualBrightness
            )
        )
    }

    companion object {
        /** PURE: pick window brightness from settings + sensor state. */
        fun resolveBrightness(
            dimmingEnabled: Boolean,
            hasSensor: Boolean,
            lastLux: Float?,
            manualBrightness: Float
        ): Float =
            if (dimmingEnabled && hasSensor && lastLux != null) {
                BrightnessMapper.luxToBrightness(lastLux)
            } else {
                manualBrightness.coerceIn(0f, 1f)
            }

        /**
         * PURE: one smoothing step. Changes inside [deadband] are ignored (kills sensor
         * jitter/flicker); larger changes ease toward the target by [alpha] per
         * [BrightnessEaser] tick (200ms, so a full swing settles in ~2s).
         */
        fun smoothed(
            previous: Float,
            target: Float,
            alpha: Float = 0.2f,
            deadband: Float = 0.05f
        ): Float =
            if (kotlin.math.abs(target - previous) < deadband) previous
            else previous + alpha * (target - previous)
    }
}
