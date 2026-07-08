package com.kisanalert.data.remote.api

import com.kisanalert.data.remote.dto.OpenWeatherForecastResponseDto
import com.kisanalert.data.remote.dto.OpenWeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") query: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherResponseDto

    @GET("data/2.5/forecast")
    suspend fun getWeatherForecast(
        @Query("q") query: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherForecastResponseDto
}
