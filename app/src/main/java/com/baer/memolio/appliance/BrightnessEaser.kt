package com.baer.memolio.appliance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Eases [brightness] toward the latest target on a fixed [tickMs] tick using
 * [AmbientDimmer.smoothed]. Ticks, not sensor events, drive the easing: the light sensor is
 * on-change and goes silent once lux stops changing. The tick loop stops once settled.
 */
class BrightnessEaser(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    initial: Float,
    private val tickMs: Long = 200L
) {
    private val _brightness = MutableStateFlow(initial)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private var target = initial
    private var easing = false

    /** Ease toward [value]; a running tick loop picks up the new target. */
    fun easeTo(value: Float) {
        synchronized(this) {
            target = value
            if (easing) return
            easing = true
        }
        scope.launch(dispatcher) {
            while (step()) delay(tickMs)
        }
    }

    /** Apply [value] at once (settings changes); a running tick loop then settles. */
    fun jumpTo(value: Float) = synchronized(this) {
        target = value
        _brightness.value = value
    }

    /** One easing step; false (and the loop ends) once inside the deadband. */
    private fun step(): Boolean = synchronized(this) {
        val current = _brightness.value
        val next = AmbientDimmer.smoothed(previous = current, target = target)
        if (next == current) {
            easing = false
            false
        } else {
            _brightness.value = next
            true
        }
    }
}
