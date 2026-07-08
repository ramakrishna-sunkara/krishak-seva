package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.WeatherAdvisoryData

interface WeatherAdvisoryRepository {
    suspend fun loadWeatherAdvisory(
        userId: String,
        forceRefresh: Boolean
    ): Result<WeatherAdvisoryData>
}
