package com.kisanalert.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherResponseDto(
    val main: OpenWeatherMainDto,
    val weather: List<OpenWeatherConditionDto>,
    val wind: OpenWeatherWindDto,
    val rain: OpenWeatherRainDto? = null,
    val name: String
)

@Serializable
data class OpenWeatherMainDto(
    val temp: Double,
    val humidity: Int
)

@Serializable
data class OpenWeatherConditionDto(
    val description: String,
    val icon: String
)

@Serializable
data class OpenWeatherWindDto(
    @SerialName("speed") val speedMetersPerSecond: Double
)

@Serializable
data class OpenWeatherRainDto(
    @SerialName("1h") val oneHourMm: Double? = null,
    @SerialName("3h") val threeHourMm: Double? = null
)
