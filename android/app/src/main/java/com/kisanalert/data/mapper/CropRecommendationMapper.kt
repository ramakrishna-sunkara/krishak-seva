package com.kisanalert.data.mapper

import com.kisanalert.data.local.entity.CropRecommendationEntity
import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.FarmingSeason

fun CropRecommendation.toEntity(
    userId: String,
    season: FarmingSeason
): CropRecommendationEntity {
    return CropRecommendationEntity(
        userId = userId,
        cropName = cropName,
        reason = reason,
        riskScore = riskScore,
        waterRequirement = waterRequirement,
        expectedYield = expectedYield,
        fertilizerAdvice = fertilizerAdvice,
        season = season.name,
        generatedAt = generatedAt,
        isFromCloud = isFromCloud
    )
}

fun CropRecommendationEntity.toDomain(): CropRecommendation {
    return CropRecommendation(
        cropName = cropName,
        reason = reason,
        riskScore = riskScore,
        waterRequirement = waterRequirement,
        expectedYield = expectedYield,
        fertilizerAdvice = fertilizerAdvice,
        generatedAt = generatedAt,
        isFromCloud = isFromCloud
    )
}
