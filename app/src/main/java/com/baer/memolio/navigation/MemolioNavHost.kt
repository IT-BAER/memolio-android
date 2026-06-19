package com.baer.memolio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * The single-activity NavHost. frameContent receives an onOpenManage lambda that
 * navigates to ManageRoute (wired here, not by the Frame screen). The nested manage
 * subroutes (LibraryRoute etc.) live inside the manage destination's own scaffold
 * (later task), so the top-level host only needs the three entry points.
 */
@Composable
fun MemolioNavHost(
    start: StartDestination,
    frameContent: @Composable (onOpenManage: () -> Unit) -> Unit,
    manageContent: @Composable () -> Unit,
    onboardContent: @Composable (onFinished: () -> Unit) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (start == StartDestination.Frame) FrameRoute else OnboardRoute
    ) {
        composable<FrameRoute> {
            frameContent { navController.navigate(ManageRoute) }
        }
        composable<OnboardRoute> {
            onboardContent {
                navController.navigate(FrameRoute) {
                    popUpTo<OnboardRoute> { inclusive = true }
                }
            }
        }
        composable<ManageRoute> {
            manageContent()
        }
    }
}
