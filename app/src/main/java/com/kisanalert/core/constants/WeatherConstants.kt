package com.kisanalert.core.constants

object WeatherConstants {
    const val USE_OPEN_METEO_PRIMARY: Boolean = true
    const val WEATHER_CACHE_TTL_MS: Long = 30L * 60L * 1000L
    const val OPEN_METEO_BASE_URL: String = "https://api.open-meteo.com/"
    const val OPEN_METEO_GEOCODING_BASE_URL: String = "https://geocoding-api.open-meteo.com/"
    const val OPEN_METEO_FORECAST_DAYS: Int = 5
}
