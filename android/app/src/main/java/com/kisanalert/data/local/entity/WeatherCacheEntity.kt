package com.kisanalert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationKey: String,
    val temperatureCelsius: Double,
    val humidityPercent: Int,
    val rainVolumeMm: Double,
    val windSpeedKmh: Double,
    val description: String,
    val cityName: String,
    val iconCode: String,
    val updatedAt: Long
)
