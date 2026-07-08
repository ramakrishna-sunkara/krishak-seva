package com.kisanalert.domain.repository

import com.kisanalert.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSessionState(): Flow<SessionState>
    suspend fun getSessionState(): SessionState
    suspend fun setAuthenticated(isAuthenticated: Boolean, userId: String?)
    suspend fun setOnboardingComplete(isComplete: Boolean)
    suspend fun clearSession()
}
