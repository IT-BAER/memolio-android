package com.baer.memolio.appliance

/**
 * Pure scheduled-sleep decision. No Android dependencies.
 *
 * Window is [startMinutes, endMinutes) in minutes-since-midnight. Handles windows
 * that wrap past midnight (start > end, e.g. 22:00 -> 07:00). start == end is a
 * zero-length window (never asleep). Inputs are normalized into [0, 1440).
 */
object SleepWindow {

    private const val DAY = 24 * 60

    fun shouldSleep(nowMinutes: Int, enabled: Boolean, startMinutes: Int, endMinutes: Int): Boolean {
        if (!enabled) return false
        val now = normalize(nowMinutes)
        val start = normalize(startMinutes)
        val end = normalize(endMinutes)
        if (start == end) return false
        return if (start < end) {
            now in start until end
        } else {
            now >= start || now < end
        }
    }

    private fun normalize(minutes: Int): Int = ((minutes % DAY) + DAY) % DAY
}
