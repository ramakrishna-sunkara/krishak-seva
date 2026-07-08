package com.kisanalert.presentation.crop

import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.FarmingSeason

data class CropRecommendationUiState(
    val selectedSeason: FarmingSeason = FarmingSeason.KHARIF,
    val recommendations: List<CropRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val isOfflineMode: Boolean = false,
    val farmerLocation: String = "",
    val currentCrop: String = "",
    val errorMessage: String? = null
)
