package com.baer.memolio.appliance

import java.util.Calendar

/**
 * Injectable "now, in minutes since local midnight" seam so sleep logic is testable
 * without a real clock. Default reads the system calendar. Distinct from
 * com.baer.memolio.core.time.TimeProvider (which serves the clock display).
 */
fun interface TimeProvider {
    fun nowMinutesSinceMidnight(): Int

    companion object {
        val System = TimeProvider {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }
    }
}
