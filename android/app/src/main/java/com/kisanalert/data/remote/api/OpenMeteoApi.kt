package com.kisanalert.data.remote.api

import com.kisanalert.data.remote.dto.OpenMeteoForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 5
    ): OpenMeteoForecastResponseDto

    private companion object {
        const val CURRENT_FIELDS: String =
            "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,weather_code"
        const val DAILY_FIELDS: String =
            "temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code"
    }
}
