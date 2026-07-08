package com.kisanalert.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kisanalert.core.constants.AppConstants
import com.kisanalert.core.notification.KisanNotificationHelper
import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.repository.AppSettingsRepository
import com.kisanalert.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KisanFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var notificationHelper: KisanNotificationHelper

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"] ?: message.notification?.title ?: DEFAULT_TITLE
        val body = message.data["message"] ?: message.notification?.body ?: return
        val alertType = parseAlertType(message.data["type"])
        val alertId = message.data["alertId"]
        val userId = message.data["userId"] ?: FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "FCM message ignored: no userId available.")
            return
        }
        serviceScope.launch {
            val alert = notificationRepository.saveIncomingAlert(
                userId = userId,
                title = title,
                message = body,
                alertType = alertType,
                alertId = alertId
            )
            val settings = appSettingsRepository.getSettings()
            if (settings.areNotificationsEnabled) {
                notificationHelper.showFarmAlertNotification(
                    alertId = alert.id,
                    title = alert.title,
                    message = alert.message,
                    alertType = alert.type
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        serviceScope.launch {
            try {
                notificationRepository.syncFcmToken(userId = userId, token = token)
            } catch (exception: Exception) {
                Log.w(TAG, "FCM token sync failed: ${exception.message}")
            }
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun parseAlertType(rawType: String?): AlertType {
        return when (rawType?.uppercase()) {
            AlertType.WEATHER.name -> AlertType.WEATHER
            AlertType.DISEASE.name -> AlertType.DISEASE
            AlertType.IRRIGATION.name -> AlertType.IRRIGATION
            else -> AlertType.WEATHER
        }
    }

    private companion object {
        const val TAG: String = "KisanFCM"
        const val DEFAULT_TITLE: String = AppConstants.APP_DISPLAY_NAME
    }
}
