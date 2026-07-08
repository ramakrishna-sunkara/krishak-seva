package com.kisanalert.presentation.weather

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.usecase.GetWeatherAdvisoryUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WeatherAdvisoryEvent {
    data object ScreenOpened : WeatherAdvisoryEvent
    data object Refresh : WeatherAdvisoryEvent
    data object DismissError : WeatherAdvisoryEvent
}

@HiltViewModel
class WeatherAdvisoryViewModel @Inject constructor(
    private val getWeatherAdvisoryUseCase: GetWeatherAdvisoryUseCase
) : MviViewModel<WeatherAdvisoryEvent, WeatherAdvisoryUiState>(
    initialState = WeatherAdvisoryUiState()
) {
    init {
        onEvent(WeatherAdvisoryEvent.ScreenOpened)
    }

    override fun onEvent(event: WeatherAdvisoryEvent) {
        when (event) {
            WeatherAdvisoryEvent.ScreenOpened -> loadWeatherAdvisory(forceRefresh = false)
            WeatherAdvisoryEvent.Refresh -> loadWeatherAdvisory(forceRefresh = true)
            WeatherAdvisoryEvent.DismissError -> {
                setState { currentState -> currentState.copy(errorMessage = null) }
            }
        }
    }

    private fun loadWeatherAdvisory(forceRefresh: Boolean) {
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isLoading = true, errorMessage = null)
            }
            when (val result = getWeatherAdvisoryUseCase.execute(forceRefresh)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        advisoryData = result.data,
                        isOfflineMode = !result.data.advisory.isFromCloud
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
