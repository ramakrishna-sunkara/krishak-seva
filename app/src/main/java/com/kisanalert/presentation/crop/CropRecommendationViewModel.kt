package com.kisanalert.presentation.crop

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.Result
import com.kisanalert.core.utils.SeasonUtils
import com.kisanalert.domain.model.FarmingSeason
import com.kisanalert.domain.usecase.GenerateCropRecommendationsUseCase
import com.kisanalert.domain.usecase.GetCurrentFarmerProfileUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CropRecommendationEvent {
    data object ScreenOpened : CropRecommendationEvent
    data class SeasonSelected(val season: FarmingSeason) : CropRecommendationEvent
    data object Refresh : CropRecommendationEvent
    data object DismissError : CropRecommendationEvent
}

@HiltViewModel
class CropRecommendationViewModel @Inject constructor(
    private val generateCropRecommendationsUseCase: GenerateCropRecommendationsUseCase,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase
) : MviViewModel<CropRecommendationEvent, CropRecommendationUiState>(
    initialState = CropRecommendationUiState(selectedSeason = SeasonUtils.getCurrentSeason())
) {
    val seasonOptions: List<FarmingSeason> = FarmingSeason.entries

    init {
        onEvent(CropRecommendationEvent.ScreenOpened)
    }

    override fun onEvent(event: CropRecommendationEvent) {
        when (event) {
            CropRecommendationEvent.ScreenOpened -> {
                loadFarmerContext()
                generateRecommendations(forceRefresh = false)
            }
            is CropRecommendationEvent.SeasonSelected -> {
                setState { currentState ->
                    currentState.copy(selectedSeason = event.season, errorMessage = null)
                }
                generateRecommendations(forceRefresh = false)
            }
            CropRecommendationEvent.Refresh -> generateRecommendations(forceRefresh = true)
            CropRecommendationEvent.DismissError -> {
                setState { currentState -> currentState.copy(errorMessage = null) }
            }
        }
    }

    private fun loadFarmerContext() {
        viewModelScope.launch {
            val profile = getCurrentFarmerProfileUseCase.execute() ?: return@launch
            setState { currentState ->
                currentState.copy(
                    farmerLocation = "${profile.village}, ${profile.district}",
                    currentCrop = profile.currentCrop
                )
            }
        }
    }

    private fun generateRecommendations(forceRefresh: Boolean) {
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isLoading = true, errorMessage = null)
            }
            val season = currentState.selectedSeason
            when (val result = generateCropRecommendationsUseCase.execute(season, forceRefresh)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        recommendations = result.data,
                        errorMessage = null,
                        isOfflineMode = result.data.any { recommendation -> !recommendation.isFromCloud }
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}
