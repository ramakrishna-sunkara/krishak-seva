package com.kisanalert.presentation.weather

import com.kisanalert.domain.model.WeatherAdvisoryData

data class WeatherAdvisoryUiState(
    val isLoading: Boolean = true,
    val advisoryData: WeatherAdvisoryData? = null,
    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false
)
