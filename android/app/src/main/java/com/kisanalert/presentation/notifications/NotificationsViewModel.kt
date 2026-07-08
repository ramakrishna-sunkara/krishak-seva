package com.kisanalert.presentation.notifications

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.usecase.GetCurrentUserIdUseCase
import com.kisanalert.domain.usecase.MarkAllNotificationsReadUseCase
import com.kisanalert.domain.usecase.MarkNotificationReadUseCase
import com.kisanalert.domain.usecase.ObserveNotificationsUseCase
import com.kisanalert.domain.usecase.SyncFcmTokenUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationsEvent {
    data object ScreenOpened : NotificationsEvent
    data class NotificationPermissionResult(val isGranted: Boolean) : NotificationsEvent
    data class FilterSelected(val filter: NotificationFilter) : NotificationsEvent
    data class AlertClicked(val alertId: String) : NotificationsEvent
    data object MarkAllRead : NotificationsEvent
    data object DismissError : NotificationsEvent
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val observeNotificationsUseCase: ObserveNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase
) : MviViewModel<NotificationsEvent, NotificationsUiState>(
    initialState = NotificationsUiState()
) {
    private var observeJob: Job? = null
    val filterOptions: List<NotificationFilter> = NotificationFilter.entries

    init {
        onEvent(NotificationsEvent.ScreenOpened)
    }

    override fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.ScreenOpened -> {
                syncFcmToken()
                observeAlerts()
            }
            is NotificationsEvent.NotificationPermissionResult -> {
                setState { currentState ->
                    currentState.copy(hasNotificationPermission = event.isGranted)
                }
            }
            is NotificationsEvent.FilterSelected -> {
                setState { currentState ->
                    currentState.copy(
                        selectedFilter = event.filter,
                        filteredAlerts = filterAlerts(
                            alerts = currentState.alerts,
                            filter = event.filter
                        )
                    )
                }
            }
            is NotificationsEvent.AlertClicked -> markAlertRead(alertId = event.alertId)
            NotificationsEvent.MarkAllRead -> markAllRead()
            NotificationsEvent.DismissError -> {
                setState { currentState -> currentState.copy(errorMessage = null) }
            }
        }
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            syncFcmTokenUseCase.execute()
        }
    }

    private fun observeAlerts() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val userId = getCurrentUserIdUseCase.execute()
            if (userId == null) {
                setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Please sign in to view notifications."
                    )
                }
                return@launch
            }
            observeNotificationsUseCase.execute(userId).collectLatest { alerts ->
                val unreadCount = alerts.count { alert -> !alert.isRead }
                setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        alerts = alerts,
                        filteredAlerts = filterAlerts(
                            alerts = alerts,
                            filter = currentState.selectedFilter
                        ),
                        unreadCount = unreadCount
                    )
                }
            }
        }
    }

    private fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            markNotificationReadUseCase.execute(alertId)
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            when (val result = markAllNotificationsReadUseCase.execute()) {
                is Result.Error -> setState { currentState ->
                    currentState.copy(errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    private fun filterAlerts(
        alerts: List<DashboardAlert>,
        filter: NotificationFilter
    ): List<DashboardAlert> {
        val alertType = filter.toAlertType() ?: return alerts
        return alerts.filter { alert -> alert.type == alertType }
    }
}
