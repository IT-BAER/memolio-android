package com.baer.memolio.feature.onboard

/** Wizard steps in order (spec 2026-07-19 rework). [Finish] is the terminal step. */
enum class OnboardStep {
    Welcome,
    ShowQr,
    Appliance,
    GoPro,
    Finish;

    val isFirst: Boolean get() = ordinal == 0
    val isLast: Boolean get() = ordinal == entries.lastIndex

    fun nextOrSelf(): OnboardStep = if (isLast) this else entries[ordinal + 1]
    fun previousOrSelf(): OnboardStep = if (isFirst) this else entries[ordinal - 1]
}
