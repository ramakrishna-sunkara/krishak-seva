package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.WeatherAdvisoryData
import com.kisanalert.domain.repository.WeatherAdvisoryRepository
import javax.inject.Inject

class GetWeatherAdvisoryUseCase @Inject constructor(
    private val weatherAdvisoryRepository: WeatherAdvisoryRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(forceRefresh: Boolean): Result<WeatherAdvisoryData> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "Please sign in to view weather advisory.")
        return weatherAdvisoryRepository.loadWeatherAdvisory(
            userId = userId,
            forceRefresh = forceRefresh
        )
    }
}
