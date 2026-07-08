package com.kisanalert.data.repository

import com.kisanalert.data.local.preferences.SessionPreferencesDataSource
import com.kisanalert.domain.model.AppSettings
import com.kisanalert.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val sessionPreferencesDataSource: SessionPreferencesDataSource
) : AppSettingsRepository {
    override fun observeSettings(): Flow<AppSettings> {
        return sessionPreferencesDataSource.observeAppSettings()
    }

    override suspend fun getSettings(): AppSettings {
        return sessionPreferencesDataSource.getAppSettings()
    }

    override suspend fun setNotificationsEnabled(isEnabled: Boolean) {
        sessionPreferencesDataSource.setNotificationsEnabled(isEnabled)
    }

    override suspend fun setLocaleCode(localeCode: String) {
        sessionPreferencesDataSource.setLocaleCode(localeCode)
    }

    override suspend fun setDashboardGuideSeen(hasSeenGuide: Boolean) {
        sessionPreferencesDataSource.setDashboardGuideSeen(hasSeenGuide)
    }
}
