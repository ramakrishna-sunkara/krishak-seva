package com.kisanalert.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kisanalert.core.constants.AppConstants
import com.kisanalert.core.constants.PreferenceKeys
import com.kisanalert.domain.model.AppSettings
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.DATASTORE_NAME
)

@Singleton
class SessionPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    fun observeSessionState(): Flow<SessionState> {
        return dataStore.data.map { preferences ->
            SessionState(
                isAuthenticated = preferences[IS_AUTHENTICATED_KEY] ?: false,
                isOnboardingComplete = preferences[IS_ONBOARDING_COMPLETE_KEY] ?: false,
                userId = preferences[USER_ID_KEY]
            )
        }
    }

    fun observeAppSettings(): Flow<AppSettings> {
        return dataStore.data.map { preferences ->
            mapAppSettings(preferences)
        }
    }

    suspend fun getSessionState(): SessionState {
        val preferences = dataStore.data.first()
        return SessionState(
            isAuthenticated = preferences[IS_AUTHENTICATED_KEY] ?: false,
            isOnboardingComplete = preferences[IS_ONBOARDING_COMPLETE_KEY] ?: false,
            userId = preferences[USER_ID_KEY]
        )
    }

    suspend fun getAppSettings(): AppSettings {
        val preferences = dataStore.data.first()
        return mapAppSettings(preferences)
    }

    suspend fun setAuthenticated(isAuthenticated: Boolean, userId: String?) {
        dataStore.edit { preferences ->
            preferences[IS_AUTHENTICATED_KEY] = isAuthenticated
            if (userId != null) {
                preferences[USER_ID_KEY] = userId
            } else {
                preferences.remove(USER_ID_KEY)
            }
        }
    }

    suspend fun setOnboardingComplete(isComplete: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETE_KEY] = isComplete
        }
    }

    suspend fun setNotificationsEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun setLocaleCode(localeCode: String) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_LANGUAGE_KEY] = localeCode
        }
    }

    suspend fun setDashboardGuideSeen(hasSeenGuide: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_DASHBOARD_GUIDE_KEY] = hasSeenGuide
        }
    }

    private fun mapAppSettings(preferences: Preferences): AppSettings {
        return AppSettings(
            areNotificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true,
            localeCode = preferences[PREFERRED_LANGUAGE_KEY] ?: PreferredLanguage.TELUGU.code,
            hasSeenDashboardGuide = preferences[HAS_SEEN_DASHBOARD_GUIDE_KEY] ?: false
        )
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val IS_AUTHENTICATED_KEY: Preferences.Key<Boolean> =
            booleanPreferencesKey(PreferenceKeys.IS_AUTHENTICATED)
        val IS_ONBOARDING_COMPLETE_KEY: Preferences.Key<Boolean> =
            booleanPreferencesKey(PreferenceKeys.IS_ONBOARDING_COMPLETE)
        val USER_ID_KEY: Preferences.Key<String> =
            stringPreferencesKey(PreferenceKeys.USER_ID)
        val NOTIFICATIONS_ENABLED_KEY: Preferences.Key<Boolean> =
            booleanPreferencesKey(PreferenceKeys.NOTIFICATIONS_ENABLED)
        val PREFERRED_LANGUAGE_KEY: Preferences.Key<String> =
            stringPreferencesKey(PreferenceKeys.PREFERRED_LANGUAGE)
        val HAS_SEEN_DASHBOARD_GUIDE_KEY: Preferences.Key<Boolean> =
            booleanPreferencesKey(PreferenceKeys.HAS_SEEN_DASHBOARD_GUIDE)
    }
}
