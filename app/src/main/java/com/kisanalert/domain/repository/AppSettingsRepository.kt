package com.kisanalert.domain.repository

import com.kisanalert.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun setNotificationsEnabled(isEnabled: Boolean)
    suspend fun setLocaleCode(localeCode: String)
    suspend fun setDashboardGuideSeen(hasSeenGuide: Boolean)
}
