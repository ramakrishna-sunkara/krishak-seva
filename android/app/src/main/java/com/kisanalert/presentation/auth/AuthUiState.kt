package com.kisanalert.presentation.auth

import com.kisanalert.domain.model.PreferredLanguage

enum class AuthStep {
    Welcome,
    PhoneOtp
}

sealed class AuthDestination {
    data object FarmerRegistration : AuthDestination()
    data object Dashboard : AuthDestination()
}

data class AuthUiState(
    val authStep: AuthStep = AuthStep.Welcome,
    val phoneNumber: String = "",
    val otpCode: String = "",
    val verificationId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val destination: AuthDestination? = null,
    val selectedLanguage: PreferredLanguage = PreferredLanguage.TELUGU,
    val shouldRecreateActivity: Boolean = false
)
