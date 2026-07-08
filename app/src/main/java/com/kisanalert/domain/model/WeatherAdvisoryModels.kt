package com.kisanalert.domain.model

data class WeatherForecastDay(
    val dayLabel: String,
    val temperatureCelsius: Double,
    val description: String,
    val humidityPercent: Int,
    val rainVolumeMm: Double,
    val iconCode: String
)

data class WeatherAdvisoryInsight(
    val summary: String,
    val irrigationAdvice: String,
    val cropRiskLevel: String,
    val tomorrowOutlook: String,
    val alertTips: List<String>,
    val isFromCloud: Boolean = true
)

data class WeatherAdvisoryData(
    val locationName: String,
    val currentCrop: String,
    val currentWeather: WeatherSummary,
    val forecastDays: List<WeatherForecastDay>,
    val advisory: WeatherAdvisoryInsight,
    val isWeatherFromCache: Boolean = false
)
