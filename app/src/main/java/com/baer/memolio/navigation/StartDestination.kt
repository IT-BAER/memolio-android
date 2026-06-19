package com.baer.memolio.navigation

/** Which top-level route the NavHost opens on launch. */
enum class StartDestination { Onboard, Frame }

/**
 * Pure selection over AppSettings.onboardingComplete. No Android deps so it unit-tests
 * without a device. The NavHost maps StartDestination.Onboard -> OnboardRoute and
 * StartDestination.Frame -> FrameRoute.
 */
fun startDestination(onboardingComplete: Boolean): StartDestination =
    if (onboardingComplete) StartDestination.Frame else StartDestination.Onboard
