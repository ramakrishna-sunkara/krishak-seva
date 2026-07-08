package com.kisanalert.data.repository

import com.kisanalert.data.local.dao.AlertDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.remote.firebase.FcmTokenDataSource
import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val alertDao: AlertDao,
    private val fcmTokenDataSource: FcmTokenDataSource
) : NotificationRepository {
    override fun observeAlerts(userId: String): Flow<List<DashboardAlert>> {
        return alertDao.observeAlerts(userId = userId, limit = 50).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override fun observeUnreadCount(userId: String): Flow<Int> {
        return alertDao.observeUnreadCount(userId)
    }

    override suspend fun markAlertAsRead(alertId: String) {
        alertDao.markAlertAsRead(alertId)
    }

    override suspend fun markAllAlertsAsRead(userId: String) {
        alertDao.markAllAlertsAsRead(userId)
    }

    override suspend fun saveIncomingAlert(
        userId: String,
        title: String,
        message: String,
        alertType: AlertType,
        alertId: String?
    ): DashboardAlert {
        val alert = DashboardAlert(
            id = alertId ?: UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = alertType,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        alertDao.insertAlert(alert.toEntity(userId))
        return alert
    }

    override suspend fun syncFcmToken(userId: String, token: String) {
        fcmTokenDataSource.saveToken(userId = userId, token = token)
    }

    override suspend fun fetchFcmToken(): String? {
        return fcmTokenDataSource.fetchToken()
    }
}
