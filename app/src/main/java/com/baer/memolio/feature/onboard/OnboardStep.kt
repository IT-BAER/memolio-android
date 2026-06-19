package com.baer.memolio.feature.onboard

/** Wizard steps in order (spec section 7). [Finish] is the terminal step. */
enum class OnboardStep {
    Welcome,
    Permissions,
    ShowQr,
    HomeKiosk,
    SleepSchedule,
    GoPro,
    Finish;

    val isFirst: Boolean get() = ordinal == 0
    val isLast: Boolean get() = ordinal == entries.lastIndex

    fun nextOrSelf(): OnboardStep = if (isLast) this else entries[ordinal + 1]
    fun previousOrSelf(): OnboardStep = if (isFirst) this else entries[ordinal - 1]
}
