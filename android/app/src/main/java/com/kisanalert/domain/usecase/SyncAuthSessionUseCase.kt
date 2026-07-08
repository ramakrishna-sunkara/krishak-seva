package com.kisanalert.domain.usecase

import com.kisanalert.domain.model.SessionState
import com.kisanalert.domain.repository.AuthRepository
import com.kisanalert.domain.repository.FarmerProfileRepository
import com.kisanalert.domain.repository.SessionRepository
import javax.inject.Inject

class SyncAuthSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val farmerProfileRepository: FarmerProfileRepository,
    private val sessionRepository: SessionRepository
) {
    suspend fun execute(): SessionState {
        val sessionState = authRepository.syncSessionWithFirebase()
        restoreOnboardingFromExistingProfile(sessionState)
        return sessionRepository.getSessionState()
    }

    private suspend fun restoreOnboardingFromExistingProfile(sessionState: SessionState) {
        if (!sessionState.isAuthenticated || sessionState.isOnboardingComplete) {
            return
        }
        val userId = sessionState.userId ?: return
        val existingProfile = farmerProfileRepository.getProfile(userId)
        if (existingProfile != null) {
            sessionRepository.setOnboardingComplete(isComplete = true)
        }
    }
}
