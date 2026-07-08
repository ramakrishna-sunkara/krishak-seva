package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.FarmingSeason
import com.kisanalert.domain.repository.CropRecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateCropRecommendationsUseCase @Inject constructor(
    private val cropRecommendationRepository: CropRecommendationRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(
        season: FarmingSeason,
        forceRefresh: Boolean = false
    ): Result<List<CropRecommendation>> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "User session not found. Please sign in again.")
        return cropRecommendationRepository.generateRecommendations(
            userId = userId,
            season = season,
            forceRefresh = forceRefresh
        )
    }
}

class ObserveCropRecommendationsUseCase @Inject constructor(
    private val cropRecommendationRepository: CropRecommendationRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(season: FarmingSeason): Flow<List<CropRecommendation>>? {
        val userId = getCurrentUserIdUseCase.execute() ?: return null
        return cropRecommendationRepository.observeRecommendations(userId, season)
    }
}
