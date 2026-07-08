package com.kisanalert.presentation.dashboard

import com.kisanalert.domain.model.DashboardData

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val dashboardData: DashboardData? = null,
    val unreadNotificationCount: Int = 0,
    val showDashboardGuide: Boolean = false,
    val errorMessage: String? = null
)
