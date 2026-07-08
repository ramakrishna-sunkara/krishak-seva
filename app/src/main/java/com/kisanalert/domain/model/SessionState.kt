package com.kisanalert.domain.model

enum class PreferredLanguage(val code: String, val displayName: String) {
    ENGLISH(code = "en", displayName = "English"),
    TELUGU(code = "te", displayName = "తెలుగు")
}

data class SessionState(
    val isAuthenticated: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val userId: String? = null
)
