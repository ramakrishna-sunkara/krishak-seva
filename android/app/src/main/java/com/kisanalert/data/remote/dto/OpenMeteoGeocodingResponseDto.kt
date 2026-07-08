package com.kisanalert.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoGeocodingResponseDto(
    val results: List<OpenMeteoGeocodingResultDto>? = null
)

@Serializable
data class OpenMeteoGeocodingResultDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country_code: String? = null,
    val admin1: String? = null
)
