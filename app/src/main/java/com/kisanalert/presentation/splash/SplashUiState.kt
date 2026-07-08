package com.kisanalert.presentation.splash

import com.kisanalert.core.constants.AppConstants
sealed class SplashDestination {
    data object Auth : SplashDestination()
    data object FarmerRegistration : SplashDestination()
    data object Dashboard : SplashDestination()
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val appName: String = AppConstants.APP_DISPLAY_NAME,
    val tagline: String = "Smart Water, Crop & Advisory System",
    val destination: SplashDestination? = null
)
