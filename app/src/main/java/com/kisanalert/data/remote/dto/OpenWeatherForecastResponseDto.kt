package com.kisanalert.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherForecastResponseDto(
    val list: List<OpenWeatherForecastItemDto>,
    val city: OpenWeatherForecastCityDto
)

@Serializable
data class OpenWeatherForecastItemDto(
    val dt: Long,
    val main: OpenWeatherMainDto,
    val weather: List<OpenWeatherConditionDto>,
    val wind: OpenWeatherWindDto,
    val rain: OpenWeatherRainDto? = null,
    @SerialName("dt_txt") val dateTimeText: String
)

@Serializable
data class OpenWeatherForecastCityDto(
    val name: String
)
