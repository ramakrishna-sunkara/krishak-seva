package com.kisanalert.domain.usecase

import com.kisanalert.domain.model.SessionState
import com.kisanalert.domain.repository.SessionRepository
import javax.inject.Inject

class GetSessionStateUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend fun execute(): SessionState {
        return sessionRepository.getSessionState()
    }
}
