package com.kisanalert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kisanalert.core.constants.NavigationRoutes
import com.kisanalert.presentation.auth.AuthScreen
import com.kisanalert.presentation.notifications.NotificationsScreen
import com.kisanalert.presentation.profile.FarmerProfileScreen
import com.kisanalert.presentation.profile.FarmerRegistrationScreen
import com.kisanalert.presentation.settings.SettingsScreen
import com.kisanalert.presentation.splash.SplashScreen
import com.kisanalert.presentation.voice.VoiceAssistantScreen

@Composable
fun KrishakSevaNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.SPLASH
    ) {
        composable(NavigationRoutes.SPLASH) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(NavigationRoutes.AUTH) {
                        popUpTo(NavigationRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToFarmerRegistration = {
                    navController.navigate(NavigationRoutes.FARMER_REGISTRATION) {
                        popUpTo(NavigationRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(NavigationRoutes.MAIN) {
                        popUpTo(NavigationRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationRoutes.AUTH) {
            AuthScreen(
                onNavigateToFarmerRegistration = {
                    navController.navigate(NavigationRoutes.FARMER_REGISTRATION) {
                        popUpTo(NavigationRoutes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(NavigationRoutes.MAIN) {
                        popUpTo(NavigationRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationRoutes.FARMER_REGISTRATION) {
            FarmerRegistrationScreen(
                onNavigateToDashboard = {
                    navController.navigate(NavigationRoutes.MAIN) {
                        popUpTo(NavigationRoutes.FARMER_REGISTRATION) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationRoutes.MAIN) { backStackEntry ->
            MainShell(
                parentBackStackEntry = backStackEntry,
                onNavigateToNotifications = {
                    navController.navigate(NavigationRoutes.NOTIFICATIONS)
                },
                onNavigateToProfile = {
                    navController.navigate(NavigationRoutes.PROFILE)
                },
                onNavigateToSettings = {
                    navController.navigate(NavigationRoutes.SETTINGS)
                },
                onNavigateToVoiceAssistant = {
                    navController.navigate(NavigationRoutes.VOICE_ASSISTANT)
                }
            )
        }
        composable(NavigationRoutes.VOICE_ASSISTANT) {
            VoiceAssistantScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavigationRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavigationRoutes.PROFILE) {
            FarmerProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAuth = {
                    navController.navigate(NavigationRoutes.AUTH) {
                        popUpTo(NavigationRoutes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
