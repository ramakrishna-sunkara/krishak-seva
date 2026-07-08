package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    fun execute(userId: String): Flow<List<DashboardAlert>> {
        return notificationRepository.observeAlerts(userId)
    }
}

class ObserveUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    fun execute(userId: String): Flow<Int> {
        return notificationRepository.observeUnreadCount(userId)
    }
}

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend fun execute(alertId: String) {
        notificationRepository.markAlertAsRead(alertId)
    }
}

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(): Result<Unit> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "Please sign in to manage notifications.")
        notificationRepository.markAllAlertsAsRead(userId)
        return Result.Success(Unit)
    }
}

class SyncFcmTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(): Result<Unit> {
        val userId = getCurrentUserIdUseCase.execute() ?: return Result.Success(Unit)
        return try {
            val token = notificationRepository.fetchFcmToken() ?: return Result.Success(Unit)
            notificationRepository.syncFcmToken(userId = userId, token = token)
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Success(Unit)
        }
    }
}

class SavePushAlertUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(
        title: String,
        message: String,
        alertType: AlertType,
        alertId: String? = null
    ): DashboardAlert? {
        val userId = getCurrentUserIdUseCase.execute() ?: return null
        return notificationRepository.saveIncomingAlert(
            userId = userId,
            title = title,
            message = message,
            alertType = alertType,
            alertId = alertId
        )
    }
}
