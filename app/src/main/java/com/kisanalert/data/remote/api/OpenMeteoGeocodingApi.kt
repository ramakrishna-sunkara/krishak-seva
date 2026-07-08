package com.kisanalert.data.remote.api

import com.kisanalert.data.remote.dto.OpenMeteoGeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("countryCode") countryCode: String = "IN",
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en"
    ): OpenMeteoGeocodingResponseDto
}
