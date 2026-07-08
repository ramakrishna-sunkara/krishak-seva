package com.kisanalert.core.utils

import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.WeatherAdvisoryInsight
import com.kisanalert.domain.model.WeatherForecastDay
import com.kisanalert.domain.model.WeatherSummary
import com.kisanalert.core.utils.DashboardAdvisor

object LocalWeatherAdvisoryAdvisor {
    fun buildAdvisory(
        profile: FarmerProfile,
        weather: WeatherSummary,
        forecastDays: List<WeatherForecastDay>
    ): WeatherAdvisoryInsight {
        val irrigationAdvice = DashboardAdvisor.buildIrrigationAdvice(profile, weather)
        val tomorrowForecast = forecastDays.drop(1).firstOrNull()
        val tomorrowRain = tomorrowForecast?.rainVolumeMm ?: 0.0
        val summary = when {
            tomorrowRain >= 2.0 -> "Rain expected tomorrow. Skip irrigation today."
            weather.rainVolumeMm >= 2.0 -> "Rain detected now. Avoid irrigation and monitor field drainage."
            weather.temperatureCelsius > 35 -> "High heat today. Irrigate ${profile.currentCrop} during early morning."
            else -> "Weather is stable. Continue regular farm activities for ${profile.currentCrop}."
        }
        val cropRiskLevel = when {
            weather.humidityPercent > 85 && weather.rainVolumeMm > 1.0 -> "High"
            weather.humidityPercent > 75 -> "Medium"
            else -> "Low"
        }
        val tomorrowOutlook = if (tomorrowForecast != null) {
            "${tomorrowForecast.dayLabel}: ${tomorrowForecast.description}, ${tomorrowForecast.temperatureCelsius.toInt()}°C"
        } else {
            "Forecast unavailable. Recheck weather later today."
        }
        val alertTips = buildAlertTips(profile, weather, tomorrowRain)
        return WeatherAdvisoryInsight(
            summary = summary,
            irrigationAdvice = irrigationAdvice.message,
            cropRiskLevel = cropRiskLevel,
            tomorrowOutlook = tomorrowOutlook,
            alertTips = alertTips,
            isFromCloud = false
        )
    }

    private fun buildAlertTips(
        profile: FarmerProfile,
        weather: WeatherSummary,
        tomorrowRain: Double
    ): List<String> {
        val tips = mutableListOf<String>()
        if (tomorrowRain >= 2.0) {
            tips.add("Cover harvested ${profile.currentCrop} to avoid moisture damage.")
        }
        if (weather.windSpeedKmh > 25) {
            tips.add("Strong winds expected. Secure farm covers and spray equipment.")
        }
        if (weather.humidityPercent > 80) {
            tips.add("High humidity increases fungal risk. Inspect ${profile.currentCrop} leaves.")
        }
        if (tips.isEmpty()) {
            tips.add("Check soil moisture before evening irrigation.")
            tips.add("Monitor local weather updates twice daily.")
        }
        return tips
    }
}
