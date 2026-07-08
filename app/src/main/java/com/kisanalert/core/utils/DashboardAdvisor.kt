package com.kisanalert.core.utils

import com.kisanalert.domain.model.CropHealthStatus
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.IrrigationAdvice
import com.kisanalert.domain.model.WaterSource
import com.kisanalert.domain.model.AiRecommendation
import com.kisanalert.domain.model.WeatherSummary

object DashboardAdvisor {
    fun calculateWaterSavingScore(profile: FarmerProfile, weather: WeatherSummary?): Int {
        val baseScore = when (profile.waterSource) {
            WaterSource.DRIP -> 92
            WaterSource.SPRINKLER -> 78
            WaterSource.CANAL -> 65
            WaterSource.BOREWELL -> 58
            WaterSource.RIVER -> 62
            WaterSource.RAIN_FED -> 70
        }
        val rainBonus = if ((weather?.rainVolumeMm ?: 0.0) > 0.0) 8 else 0
        return (baseScore + rainBonus).coerceAtMost(100)
    }

    fun calculateCropHealth(profile: FarmerProfile, weather: WeatherSummary?): CropHealthStatus {
        val humidity = weather?.humidityPercent ?: 0
        val rain = weather?.rainVolumeMm ?: 0.0
        return when {
            humidity > 85 && rain > 2.0 -> CropHealthStatus.WARNING
            humidity > 90 -> CropHealthStatus.CRITICAL
            else -> CropHealthStatus.GOOD
        }
    }

    fun buildIrrigationAdvice(profile: FarmerProfile, weather: WeatherSummary?): IrrigationAdvice {
        val rain = weather?.rainVolumeMm ?: 0.0
        val humidity = weather?.humidityPercent ?: 0
        return when {
            rain >= 2.0 -> IrrigationAdvice(
                title = "Skip Irrigation Today",
                message = "Rain detected in your area. Save water and skip irrigation to prevent waterlogging for ${profile.currentCrop}.",
                shouldIrrigateToday = false
            )
            humidity > 80 -> IrrigationAdvice(
                title = "Reduce Irrigation",
                message = "High humidity (${humidity}%). Reduce watering for ${profile.currentCrop} and monitor soil moisture.",
                shouldIrrigateToday = false
            )
            (weather?.temperatureCelsius ?: 0.0) > 35 -> IrrigationAdvice(
                title = "Irrigate Early Morning",
                message = "High temperature expected. Irrigate ${profile.currentCrop} early morning or evening to reduce evaporation.",
                shouldIrrigateToday = true
            )
            else -> IrrigationAdvice(
                title = "Normal Irrigation",
                message = "Weather conditions are favorable. Follow your regular schedule for ${profile.currentCrop}.",
                shouldIrrigateToday = true
            )
        }
    }

    fun buildAiRecommendation(profile: FarmerProfile, weather: WeatherSummary?): AiRecommendation {
        val cropName = profile.currentCrop
        val message = if ((weather?.rainVolumeMm ?: 0.0) > 0.0) {
            "Rain expected near ${profile.village}. Consider delaying fertilizer application for $cropName by 2–3 days."
        } else {
            "Based on your ${profile.soilType.displayName.lowercase()} soil and ${profile.waterSource.displayName.lowercase()}, $cropName is performing well this season. Monitor for pests in the next week."
        }
        return AiRecommendation(
            cropName = cropName,
            title = "AI Insight for $cropName",
            message = message,
            actionLabel = "View Crop Advice"
        )
    }
}
