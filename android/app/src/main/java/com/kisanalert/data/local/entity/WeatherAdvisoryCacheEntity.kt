package com.kisanalert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_advisory_cache")
data class WeatherAdvisoryCacheEntity(
    @PrimaryKey val locationKey: String,
    val summary: String,
    val irrigationAdvice: String,
    val cropRiskLevel: String,
    val tomorrowOutlook: String,
    val alertTipsJson: String,
    val isFromCloud: Boolean,
    val updatedAt: Long
)
