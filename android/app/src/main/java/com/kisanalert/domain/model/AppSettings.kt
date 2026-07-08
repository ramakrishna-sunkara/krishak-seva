package com.kisanalert.domain.model

data class AppSettings(
    val areNotificationsEnabled: Boolean = true,
    val localeCode: String = PreferredLanguage.TELUGU.code,
    val hasSeenDashboardGuide: Boolean = false
)
