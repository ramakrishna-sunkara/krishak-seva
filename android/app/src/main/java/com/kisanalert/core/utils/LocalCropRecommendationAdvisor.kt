package com.kisanalert.core.utils

import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.FarmingSeason
import com.kisanalert.domain.model.GroundWaterCategory
import com.kisanalert.domain.model.GroundWaterDistrictAssessment
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource
import java.util.Calendar

object SeasonUtils {
    fun getCurrentSeason(): FarmingSeason {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return when (month) {
            in 6..9 -> FarmingSeason.KHARIF
            in 10..12, 1 -> FarmingSeason.RABI
            else -> FarmingSeason.ZAID
        }
    }
}

object LocalCropRecommendationAdvisor {
    fun generateRecommendations(
        profile: FarmerProfile,
        season: FarmingSeason,
        groundWaterAssessment: GroundWaterDistrictAssessment? = null
    ): List<CropRecommendation> {
        val recommendations = mutableListOf<CropRecommendation>()
        val primaryCrop = buildPrimaryRecommendation(profile, season, groundWaterAssessment)
        recommendations.add(primaryCrop)
        recommendations.add(buildSecondaryRecommendation(profile, season, groundWaterAssessment))
        if (profile.waterSource == WaterSource.RAIN_FED ||
            isGroundWaterStressed(groundWaterAssessment)
        ) {
            recommendations.add(buildRainFedRecommendation(profile, season))
        }
        return recommendations.distinctBy { recommendation -> recommendation.cropName }
    }

    private fun buildPrimaryRecommendation(
        profile: FarmerProfile,
        season: FarmingSeason,
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): CropRecommendation {
        val cropName = resolvePrimaryCropName(profile, season, groundWaterAssessment)
        val groundWaterNote = buildGroundWaterNote(groundWaterAssessment)
        return CropRecommendation(
            cropName = cropName,
            reason = "Suitable for ${profile.soilType.displayName} soil in ${profile.district} during ${season.displayName}.$groundWaterNote",
            riskScore = calculateRiskScore(profile, groundWaterAssessment),
            waterRequirement = mapWaterRequirement(profile.waterSource, groundWaterAssessment),
            expectedYield = estimateYield(cropName, profile.farmSizeAcres),
            fertilizerAdvice = buildFertilizerAdvice(cropName, profile.soilType),
            isFromCloud = false
        )
    }

    private fun buildSecondaryRecommendation(
        profile: FarmerProfile,
        season: FarmingSeason,
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): CropRecommendation {
        val cropName = when {
            isGroundWaterStressed(groundWaterAssessment) -> "Groundnut"
            season == FarmingSeason.KHARIF -> "Groundnut"
            season == FarmingSeason.RABI -> "Mustard"
            else -> "Green Gram"
        }
        return CropRecommendation(
            cropName = cropName,
            reason = "Good alternative with ${profile.waterSource.displayName.lowercase()} and moderate input cost for your region.",
            riskScore = (calculateRiskScore(profile, groundWaterAssessment) + 10).coerceAtMost(90),
            waterRequirement = if (isGroundWaterStressed(groundWaterAssessment)) "Low" else "Low to Medium",
            expectedYield = estimateYield(cropName, profile.farmSizeAcres),
            fertilizerAdvice = "Apply well-decomposed farmyard manure before sowing; split nitrogen doses.",
            isFromCloud = false
        )
    }

    private fun buildRainFedRecommendation(
        profile: FarmerProfile,
        season: FarmingSeason
    ): CropRecommendation {
        return CropRecommendation(
            cropName = if (season == FarmingSeason.KHARIF) "Bajra" else "Barley",
            reason = "Drought-tolerant option for rain-fed farms in ${profile.state}.",
            riskScore = 20,
            waterRequirement = "Low",
            expectedYield = estimateYield("Bajra", profile.farmSizeAcres),
            fertilizerAdvice = "Use phosphorus-rich basal dose; avoid excess nitrogen in dry spells.",
            isFromCloud = false
        )
    }

    private fun calculateRiskScore(
        profile: FarmerProfile,
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): Int {
        var risk = 30
        if (profile.waterSource == WaterSource.RAIN_FED) {
            risk += 20
        }
        if (profile.farmSizeAcres < 1.0) {
            risk += 10
        }
        when (groundWaterAssessment?.category) {
            GroundWaterCategory.SEMI_CRITICAL -> risk += 10
            GroundWaterCategory.CRITICAL -> risk += 20
            GroundWaterCategory.OVER_EXPLOITED -> risk += 30
            else -> Unit
        }
        return risk.coerceIn(10, 90)
    }

    private fun mapWaterRequirement(
        waterSource: WaterSource,
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): String {
        if (isGroundWaterStressed(groundWaterAssessment)) {
            return "Low"
        }
        return when (waterSource) {
            WaterSource.DRIP, WaterSource.SPRINKLER -> "Low"
            WaterSource.CANAL, WaterSource.RIVER -> "Medium"
            WaterSource.BOREWELL -> "Medium-High"
            WaterSource.RAIN_FED -> "Low"
        }
    }

    private fun resolvePrimaryCropName(
        profile: FarmerProfile,
        season: FarmingSeason,
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): String {
        if (isGroundWaterStressed(groundWaterAssessment)) {
            return when (season) {
                FarmingSeason.KHARIF -> "Bajra"
                FarmingSeason.RABI -> "Chickpea"
                FarmingSeason.ZAID -> "Green Gram"
            }
        }
        return when (season) {
            FarmingSeason.KHARIF -> when (profile.soilType) {
                SoilType.BLACK -> "Cotton"
                SoilType.ALLUVIAL -> "Rice"
                SoilType.RED -> "Maize"
                else -> profile.currentCrop.ifBlank { "Pulses" }
            }
            FarmingSeason.RABI -> when (profile.soilType) {
                SoilType.BLACK -> "Wheat"
                SoilType.ALLUVIAL -> "Wheat"
                else -> "Chickpea"
            }
            FarmingSeason.ZAID -> "Vegetables"
        }
    }

    private fun isGroundWaterStressed(
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): Boolean {
        return when (groundWaterAssessment?.category) {
            GroundWaterCategory.SEMI_CRITICAL,
            GroundWaterCategory.CRITICAL,
            GroundWaterCategory.OVER_EXPLOITED -> true
            else -> false
        }
    }

    private fun buildGroundWaterNote(
        groundWaterAssessment: GroundWaterDistrictAssessment?
    ): String {
        if (groundWaterAssessment == null) {
            return ""
        }
        return " District groundwater is ${groundWaterAssessment.category.toDisplayName().lowercase()} " +
            "(${groundWaterAssessment.stageOfExtractionPercent}% stage of extraction)."
    }

    private fun estimateYield(cropName: String, farmSizeAcres: Double): String {
        val perAcre = when (cropName.lowercase()) {
            "rice", "paddy" -> "20-25 quintals/acre"
            "cotton" -> "8-12 quintals/acre"
            "wheat" -> "18-22 quintals/acre"
            "maize" -> "15-18 quintals/acre"
            "groundnut" -> "10-14 quintals/acre"
            else -> "8-15 quintals/acre"
        }
        val totalQuintalsLow = (farmSizeAcres * 8).toInt()
        val totalQuintalsHigh = (farmSizeAcres * 15).toInt()
        return "$perAcre (farm total ~$totalQuintalsLow-$totalQuintalsHigh qtl)"
    }

    private fun buildFertilizerAdvice(cropName: String, soilType: SoilType): String {
        return when {
            cropName.equals("Cotton", ignoreCase = true) -> "Apply NPK 120:60:60 kg/ha in split doses; add zinc in ${soilType.displayName} soils."
            cropName.equals("Rice", ignoreCase = true) -> "Basal DAP + urea top dressing at tillering and panicle initiation."
            cropName.equals("Wheat", ignoreCase = true) -> "Apply DAP at sowing; two urea splits at crown root and flowering."
            else -> "Use soil test based NPK; combine organic manure with micronutrients."
        }
    }
}
