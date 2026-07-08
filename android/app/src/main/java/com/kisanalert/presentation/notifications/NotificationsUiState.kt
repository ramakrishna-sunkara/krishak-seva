package com.kisanalert.presentation.notifications

import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.DashboardAlert

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val hasNotificationPermission: Boolean = false,
    val alerts: List<DashboardAlert> = emptyList(),
    val filteredAlerts: List<DashboardAlert> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val unreadCount: Int = 0,
    val errorMessage: String? = null
)

enum class NotificationFilter(val displayName: String) {
    ALL(displayName = "All"),
    WEATHER(displayName = "Weather"),
    IRRIGATION(displayName = "Irrigation"),
    DISEASE(displayName = "Disease")
}

fun NotificationFilter.toAlertType(): AlertType? {
    return when (this) {
        NotificationFilter.ALL -> null
        NotificationFilter.WEATHER -> AlertType.WEATHER
        NotificationFilter.IRRIGATION -> AlertType.IRRIGATION
        NotificationFilter.DISEASE -> AlertType.DISEASE
    }
}
