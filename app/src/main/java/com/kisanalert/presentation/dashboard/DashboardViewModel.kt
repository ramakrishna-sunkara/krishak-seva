package com.kisanalert.presentation.dashboard

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.usecase.GetCurrentUserIdUseCase
import com.kisanalert.domain.usecase.GetDashboardDataUseCase
import com.kisanalert.domain.usecase.MarkDashboardGuideSeenUseCase
import com.kisanalert.domain.usecase.ObserveAppSettingsUseCase
import com.kisanalert.domain.usecase.ObserveUnreadNotificationCountUseCase
import com.kisanalert.domain.usecase.SyncFcmTokenUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DashboardEvent {
    data object ScreenOpened : DashboardEvent
    data object Refresh : DashboardEvent
    data object Retry : DashboardEvent
    data object DismissError : DashboardEvent
    data object DismissDashboardGuide : DashboardEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val observeUnreadNotificationCountUseCase: ObserveUnreadNotificationCountUseCase,
    private val observeAppSettingsUseCase: ObserveAppSettingsUseCase,
    private val markDashboardGuideSeenUseCase: MarkDashboardGuideSeenUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase
) : MviViewModel<DashboardEvent, DashboardUiState>(
    initialState = DashboardUiState()
) {
    private var unreadObserveJob: Job? = null
    private var settingsObserveJob: Job? = null

    init {
        onEvent(DashboardEvent.ScreenOpened)
    }

    override fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.ScreenOpened -> {
                syncFcmToken()
                observeUnreadCount()
                observeDashboardGuide()
                loadDashboard(forceRefresh = true)
            }
            DashboardEvent.Refresh -> loadDashboard(forceRefresh = true, isRefreshing = true)
            DashboardEvent.Retry -> loadDashboard(forceRefresh = true)
            DashboardEvent.DismissError -> {
                setState { currentState -> currentState.copy(errorMessage = null) }
            }
            DashboardEvent.DismissDashboardGuide -> dismissDashboardGuide()
        }
    }

    private fun observeDashboardGuide() {
        settingsObserveJob?.cancel()
        settingsObserveJob = viewModelScope.launch {
            observeAppSettingsUseCase.execute().collectLatest { settings ->
                setState { currentState ->
                    currentState.copy(showDashboardGuide = !settings.hasSeenDashboardGuide)
                }
            }
        }
    }

    private fun dismissDashboardGuide() {
        viewModelScope.launch {
            markDashboardGuideSeenUseCase.execute()
            setState { currentState ->
                currentState.copy(showDashboardGuide = false)
            }
        }
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            syncFcmTokenUseCase.execute()
        }
    }

    private fun observeUnreadCount() {
        unreadObserveJob?.cancel()
        unreadObserveJob = viewModelScope.launch {
            val userId = getCurrentUserIdUseCase.execute() ?: return@launch
            observeUnreadNotificationCountUseCase.execute(userId).collectLatest { count ->
                setState { currentState ->
                    currentState.copy(unreadNotificationCount = count)
                }
            }
        }
    }

    private fun loadDashboard(forceRefresh: Boolean, isRefreshing: Boolean = false) {
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(
                    isLoading = !isRefreshing && currentState.dashboardData == null,
                    isRefreshing = isRefreshing,
                    errorMessage = null
                )
            }
            when (val result = getDashboardDataUseCase.execute(forceRefresh = forceRefresh)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        dashboardData = result.data
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}
