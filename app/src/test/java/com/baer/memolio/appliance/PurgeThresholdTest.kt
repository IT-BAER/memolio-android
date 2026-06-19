package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PurgeThresholdTest {

    private val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

    @Test
    fun thresholdIs30DaysBeforeNow() {
        val now = 1_000_000_000_000L
        assertThat(PurgeThreshold.cutoff(now)).isEqualTo(now - thirtyDaysMillis)
    }

    @Test
    fun customRetentionDaysRespected() {
        val now = 2_000_000_000_000L
        val sevenDays = 7L * 24 * 60 * 60 * 1000
        assertThat(PurgeThreshold.cutoff(now, retentionDays = 7)).isEqualTo(now - sevenDays)
    }

    @Test
    fun zeroNowGivesNegativeCutoff() {
        assertThat(PurgeThreshold.cutoff(0L)).isEqualTo(-thirtyDaysMillis)
    }
}
