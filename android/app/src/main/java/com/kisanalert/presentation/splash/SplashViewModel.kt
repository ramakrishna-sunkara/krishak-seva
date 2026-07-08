package com.kisanalert.presentation.splash

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.constants.AppConstants
import com.kisanalert.domain.usecase.GetSessionStateUseCase
import com.kisanalert.domain.usecase.SyncAuthSessionUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashEvent {
    data object ScreenOpened : SplashEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val syncAuthSessionUseCase: SyncAuthSessionUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase
) : MviViewModel<SplashEvent, SplashUiState>(
    initialState = SplashUiState()
) {
    init {
        onEvent(SplashEvent.ScreenOpened)
    }

    override fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.ScreenOpened -> resolveStartupDestination()
        }
    }

    private fun resolveStartupDestination() {
        viewModelScope.launch {
            delay(AppConstants.SPLASH_DELAY_MS)
            syncAuthSessionUseCase.execute()
            val sessionState = getSessionStateUseCase.execute()
            val destination = when {
                !sessionState.isAuthenticated -> SplashDestination.Auth
                !sessionState.isOnboardingComplete -> SplashDestination.FarmerRegistration
                else -> SplashDestination.Dashboard
            }
            setState { currentState ->
                currentState.copy(
                    isLoading = false,
                    destination = destination
                )
            }
        }
    }
}
