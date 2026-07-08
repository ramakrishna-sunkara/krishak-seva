package com.kisanalert.presentation.settings

import com.kisanalert.domain.model.PreferredLanguage

data class SettingsUiState(
    val isLoading: Boolean = true,
    val preferredLanguage: PreferredLanguage = PreferredLanguage.TELUGU,
    val areNotificationsEnabled: Boolean = true,
    val hasNotificationPermission: Boolean = true,
    val isUpdatingLanguage: Boolean = false,
    val isSigningOut: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val appVersion: String = "",
    val errorMessage: String? = null,
    val signedOut: Boolean = false,
    val shouldRecreateActivity: Boolean = false
)
