package com.baer.memolio.appliance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SleepWindowTest {

    @Test
    fun disabledIsNeverAsleep() {
        assertThat(SleepWindow.shouldSleep(nowMinutes = 23 * 60, enabled = false, startMinutes = 22 * 60, endMinutes = 7 * 60)).isFalse()
    }

    @Test
    fun wrappingWindowAsleepBeforeMidnight() {
        assertThat(SleepWindow.shouldSleep(23 * 60, true, 22 * 60, 7 * 60)).isTrue()
    }

    @Test
    fun wrappingWindowAsleepAfterMidnight() {
        assertThat(SleepWindow.shouldSleep(3 * 60, true, 22 * 60, 7 * 60)).isTrue()
    }

    @Test
    fun wrappingWindowAwakeMidday() {
        assertThat(SleepWindow.shouldSleep(12 * 60, true, 22 * 60, 7 * 60)).isFalse()
    }

    @Test
    fun wrappingWindowStartIsInclusive() {
        assertThat(SleepWindow.shouldSleep(22 * 60, true, 22 * 60, 7 * 60)).isTrue()
    }

    @Test
    fun wrappingWindowEndIsExclusive() {
        assertThat(SleepWindow.shouldSleep(7 * 60, true, 22 * 60, 7 * 60)).isFalse()
    }

    @Test
    fun sameDayWindowAsleepInside() {
        assertThat(SleepWindow.shouldSleep(3 * 60, true, 1 * 60, 6 * 60)).isTrue()
    }

    @Test
    fun sameDayWindowAwakeOutside() {
        assertThat(SleepWindow.shouldSleep(9 * 60, true, 1 * 60, 6 * 60)).isFalse()
    }

    @Test
    fun sameDayStartInclusiveEndExclusive() {
        assertThat(SleepWindow.shouldSleep(1 * 60, true, 1 * 60, 6 * 60)).isTrue()
        assertThat(SleepWindow.shouldSleep(6 * 60, true, 1 * 60, 6 * 60)).isFalse()
    }

    @Test
    fun emptyWindowStartEqualsEndIsNeverAsleep() {
        assertThat(SleepWindow.shouldSleep(5 * 60, true, 8 * 60, 8 * 60)).isFalse()
    }

    @Test
    fun outOfRangeMinutesAreNormalized() {
        assertThat(SleepWindow.shouldSleep(25 * 60, true, 1 * 60, 6 * 60)).isTrue()
    }
}
