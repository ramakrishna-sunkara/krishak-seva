package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.FarmingSeason
import kotlinx.coroutines.flow.Flow

interface CropRecommendationRepository {
    fun observeRecommendations(userId: String, season: FarmingSeason): Flow<List<CropRecommendation>>
    suspend fun getRecommendations(userId: String, season: FarmingSeason): List<CropRecommendation>
    suspend fun generateRecommendations(
        userId: String,
        season: FarmingSeason,
        forceRefresh: Boolean = false
    ): Result<List<CropRecommendation>>
}
