package com.kisanalert.core.constants

object AppConstants {
    const val APP_DISPLAY_NAME: String = "कृषकसेवा"
    const val SPLASH_DELAY_MS: Long = 2000L
    const val DATABASE_NAME: String = "krishak_seva_db"
    const val DATASTORE_NAME: String = "krishak_seva_prefs"
    const val FIRESTORE_SYNC_TIMEOUT_MS: Long = 8_000L
    const val STORAGE_UPLOAD_TIMEOUT_MS: Long = 30_000L
    const val CLOUD_FUNCTION_TIMEOUT_MS: Long = 45_000L
}

object NavigationRoutes {
    const val SPLASH: String = "splash"
    const val AUTH: String = "auth"
    const val FARMER_REGISTRATION: String = "farmer_registration"
    const val MAIN: String = "main"
    const val DASHBOARD: String = "dashboard"
    const val CROP_RECOMMENDATION: String = "crop_recommendation"
    const val WEATHER_ADVISORY: String = "weather_advisory"
    const val CROP_DOCTOR: String = "crop_doctor"
    const val VOICE_ASSISTANT: String = "voice_assistant"
    const val NOTIFICATIONS: String = "notifications"
    const val PROFILE: String = "profile"
    const val SETTINGS: String = "settings"
}

object BottomNavRoutes {
    const val HOME: String = "tab_home"
    const val CROPS: String = "tab_crops"
    const val DOCTOR: String = "tab_doctor"
    const val WEATHER: String = "tab_weather"
    const val ACCOUNT: String = "tab_account"
}

object PreferenceKeys {
    const val IS_ONBOARDING_COMPLETE: String = "is_onboarding_complete"
    const val IS_AUTHENTICATED: String = "is_authenticated"
    const val USER_ID: String = "user_id"
    const val PREFERRED_LANGUAGE: String = "preferred_language"
    const val NOTIFICATIONS_ENABLED: String = "notifications_enabled"
    const val HAS_SEEN_DASHBOARD_GUIDE: String = "has_seen_dashboard_guide"
}

object CloudFunctionEndpoints {
    const val CROP_RECOMMENDATION: String = "getCropRecommendation"
    const val WEATHER_ADVISORY: String = "getWeatherAdvisory"
    const val DISEASE_DETECTION: String = "detectCropDisease"
    const val UPLOAD_CROP_SCAN_IMAGE: String = "uploadCropScanImage"
    const val VOICE_ASSISTANT: String = "askFarmingQuestion"
}
