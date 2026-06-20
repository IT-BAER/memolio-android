package com.baer.memolio.navigation

/** Which top-level route the NavHost opens on launch. */
enum class StartDestination { Onboard, Frame }

/**
 * Pure selection over AppSettings.onboardingComplete. No Android deps so it unit-tests
 * without a device. The NavHost maps StartDestination.Onboard -> OnboardRoute and
 * StartDestination.Frame -> FrameRoute.
 *
 * [onboardingComplete] is null while the persisted flag is still loading; we return
 * null (undecided) so the caller renders nothing rather than flashing Onboard before
 * the real value arrives.
 */
fun startDestination(onboardingComplete: Boolean?): StartDestination? = when (onboardingComplete) {
    null -> null
    true -> StartDestination.Frame
    false -> StartDestination.Onboard
}
