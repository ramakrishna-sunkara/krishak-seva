package com.kisanalert.data.repository

import com.kisanalert.data.local.preferences.SessionPreferencesDataSource
import com.kisanalert.domain.model.SessionState
import com.kisanalert.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionPreferencesDataSource: SessionPreferencesDataSource
) : SessionRepository {
    override fun observeSessionState(): Flow<SessionState> {
        return sessionPreferencesDataSource.observeSessionState()
    }

    override suspend fun getSessionState(): SessionState {
        return sessionPreferencesDataSource.getSessionState()
    }

    override suspend fun setAuthenticated(isAuthenticated: Boolean, userId: String?) {
        sessionPreferencesDataSource.setAuthenticated(isAuthenticated, userId)
    }

    override suspend fun setOnboardingComplete(isComplete: Boolean) {
        sessionPreferencesDataSource.setOnboardingComplete(isComplete)
    }

    override suspend fun clearSession() {
        sessionPreferencesDataSource.clearSession()
    }
}
