package com.kisanalert.domain.model

enum class GroundWaterCategory {
    SAFE,
    SEMI_CRITICAL,
    CRITICAL,
    OVER_EXPLOITED;

    fun toDisplayName(): String {
        return when (this) {
            SAFE -> "Safe"
            SEMI_CRITICAL -> "Semi-critical"
            CRITICAL -> "Critical"
            OVER_EXPLOITED -> "Over-exploited"
        }
    }

    companion object {
        fun fromStagePercent(stagePercent: Double): GroundWaterCategory {
            return when {
                stagePercent > 100.0 -> OVER_EXPLOITED
                stagePercent >= 90.0 -> CRITICAL
                stagePercent >= 70.0 -> SEMI_CRITICAL
                else -> SAFE
            }
        }
    }
}

data class GroundWaterDistrictAssessment(
    val state: String,
    val district: String,
    val stageOfExtractionPercent: Double,
    val category: GroundWaterCategory,
    val annualExtractableResourceHam: Double,
    val totalAnnualExtractionHam: Double,
    val netAvailabilityForFutureHam: Double,
    val totalAnnualRechargeHam: Double,
    val assessmentYear: String,
    val source: String
)
