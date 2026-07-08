package com.kisanalert.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kisanalert.core.constants.BottomNavRoutes
import com.kisanalert.core.constants.NavigationRoutes
import com.kisanalert.core.ui.components.KisanBottomNavBar
import com.kisanalert.presentation.account.AccountHubScreen
import com.kisanalert.presentation.crop.CropRecommendationScreen
import com.kisanalert.presentation.cropdoctor.CropDoctorScreen
import com.kisanalert.presentation.dashboard.DashboardScreen
import com.kisanalert.presentation.dashboard.DashboardViewModel
import com.kisanalert.presentation.weather.WeatherAdvisoryScreen
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainShell(
    parentBackStackEntry: NavBackStackEntry,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVoiceAssistant: () -> Unit,
    bottomNavController: NavHostController = rememberNavController()
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel(parentBackStackEntry)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute: String? = navBackStackEntry?.destination?.route
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            KisanBottomNavBar(
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavRoutes.HOME) {
                DashboardScreen(
                    isTabRoot = true,
                    viewModel = dashboardViewModel,
                    onNavigateToCropRecommendation = {
                        bottomNavController.navigate(BottomNavRoutes.CROPS) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToWeatherAdvisory = {
                        bottomNavController.navigate(BottomNavRoutes.WEATHER) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCropDoctor = {
                        bottomNavController.navigate(BottomNavRoutes.DOCTOR) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToVoiceAssistant = onNavigateToVoiceAssistant,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            composable(BottomNavRoutes.CROPS) {
                CropRecommendationScreen(
                    isTabRoot = true,
                    onNavigateBack = {
                        bottomNavController.navigate(BottomNavRoutes.HOME) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavRoutes.DOCTOR) {
                CropDoctorScreen(
                    isTabRoot = true,
                    onNavigateBack = {
                        bottomNavController.navigate(BottomNavRoutes.HOME) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavRoutes.WEATHER) {
                WeatherAdvisoryScreen(
                    isTabRoot = true,
                    onNavigateBack = {
                        bottomNavController.navigate(BottomNavRoutes.HOME) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavRoutes.ACCOUNT) {
                AccountHubScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToVoiceAssistant = onNavigateToVoiceAssistant
                )
            }
        }
    }
}
