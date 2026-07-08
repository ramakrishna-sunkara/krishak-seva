package com.kisanalert.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kisanalert.MainActivity
import com.kisanalert.R
import com.kisanalert.domain.model.AlertType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KisanNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    fun showFarmAlertNotification(
        alertId: String,
        title: String,
        message: String,
        alertType: AlertType
    ) {
        createNotificationChannels()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALERT_ID, alertId)
            putExtra(EXTRA_OPEN_NOTIFICATIONS, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(mapCategory(alertType))
            .build()
        NotificationManagerCompat.from(context).notify(alertId.hashCode(), notification)
    }

    private fun mapCategory(alertType: AlertType): String {
        return when (alertType) {
            AlertType.WEATHER -> NotificationCompat.CATEGORY_STATUS
            AlertType.IRRIGATION -> NotificationCompat.CATEGORY_REMINDER
            AlertType.DISEASE -> NotificationCompat.CATEGORY_ALARM
        }
    }

    companion object {
        const val CHANNEL_ID: String = "farm_alerts"
        const val EXTRA_ALERT_ID: String = "extra_alert_id"
        const val EXTRA_OPEN_NOTIFICATIONS: String = "extra_open_notifications"
    }
}
