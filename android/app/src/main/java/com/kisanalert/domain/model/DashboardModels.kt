package com.kisanalert.domain.model

enum class AlertType {
    WEATHER,
    DISEASE,
    IRRIGATION
}

enum class CropHealthStatus(val displayName: String) {
    GOOD(displayName = "Healthy"),
    WARNING(displayName = "Needs Attention"),
    CRITICAL(displayName = "At Risk")
}

data class WeatherSummary(
    val temperatureCelsius: Double,
    val humidityPercent: Int,
    val rainVolumeMm: Double,
    val windSpeedKmh: Double,
    val description: String,
    val cityName: String,
    val iconCode: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class IrrigationAdvice(
    val title: String,
    val message: String,
    val shouldIrrigateToday: Boolean
)

data class AiRecommendation(
    val cropName: String,
    val title: String,
    val message: String,
    val actionLabel: String
)

data class DashboardAlert(
    val id: String,
    val title: String,
    val message: String,
    val type: AlertType,
    val timestamp: Long,
    val isRead: Boolean = false
)

data class DashboardData(
    val farmerName: String,
    val village: String,
    val currentCrop: String,
    val weather: WeatherSummary?,
    val waterSavingScore: Int,
    val cropHealthStatus: CropHealthStatus,
    val irrigationAdvice: IrrigationAdvice,
    val aiRecommendation: AiRecommendation,
    val recentAlerts: List<DashboardAlert>,
    val isWeatherFromCache: Boolean = false
)
