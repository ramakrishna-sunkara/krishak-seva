package com.kisanalert.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoForecastResponseDto(
    val latitude: Double,
    val longitude: Double,
    val current: OpenMeteoCurrentDto,
    val daily: OpenMeteoDailyDto
)

@Serializable
data class OpenMeteoCurrentDto(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val precipitation: Double,
    val wind_speed_10m: Double,
    val weather_code: Int
)

@Serializable
data class OpenMeteoDailyDto(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val precipitation_sum: List<Double>,
    val weather_code: List<Int>
)
