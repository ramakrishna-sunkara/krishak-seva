package com.kisanalert.domain.repository

import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.DashboardAlert
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeAlerts(userId: String): Flow<List<DashboardAlert>>
    fun observeUnreadCount(userId: String): Flow<Int>
    suspend fun markAlertAsRead(alertId: String)
    suspend fun markAllAlertsAsRead(userId: String)
    suspend fun saveIncomingAlert(
        userId: String,
        title: String,
        message: String,
        alertType: AlertType,
        alertId: String? = null
    ): DashboardAlert
    suspend fun syncFcmToken(userId: String, token: String)
    suspend fun fetchFcmToken(): String?
}
