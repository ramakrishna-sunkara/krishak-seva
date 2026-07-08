package com.kisanalert.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CropRecommendation(
    val cropName: String,
    val reason: String,
    val riskScore: Int,
    val waterRequirement: String,
    val expectedYield: String,
    val fertilizerAdvice: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val isFromCloud: Boolean = true
)

@Serializable
data class CropRecommendationRequest(
    val userId: String,
    val farmerName: String,
    val village: String,
    val district: String,
    val state: String,
    val farmSizeAcres: Double,
    val soilType: String,
    val waterSource: String,
    val currentCrop: String,
    val season: String,
    val temperatureCelsius: Double?,
    val humidityPercent: Int?,
    val rainVolumeMm: Double?,
    val languageCode: String = PreferredLanguage.TELUGU.code,
    val groundWaterCategory: String? = null,
    val groundWaterStagePercent: Double? = null,
    val groundWaterAssessmentYear: String? = null
)

enum class FarmingSeason(val displayName: String) {
    KHARIF(displayName = "Kharif (Monsoon)"),
    RABI(displayName = "Rabi (Winter)"),
    ZAID(displayName = "Zaid (Summer)")
}
