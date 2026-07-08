package com.kisanalert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_recommendations")
data class CropRecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val cropName: String,
    val reason: String,
    val riskScore: Int,
    val waterRequirement: String,
    val expectedYield: String,
    val fertilizerAdvice: String,
    val season: String,
    val generatedAt: Long,
    val isFromCloud: Boolean
)
