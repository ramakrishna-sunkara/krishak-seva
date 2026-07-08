package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.repository.AuthRepository
import com.kisanalert.domain.repository.FarmerProfileRepository
import com.kisanalert.domain.repository.SessionRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend fun execute(): String? {
        val authUserId = authRepository.getCurrentUser()?.userId
        if (authUserId != null) {
            return authUserId
        }
        return sessionRepository.getSessionState().userId
    }
}

class SaveFarmerProfileUseCase @Inject constructor(
    private val farmerProfileRepository: FarmerProfileRepository,
    private val sessionRepository: SessionRepository
) {
    suspend fun execute(profile: FarmerProfile): Result<FarmerProfile> {
        val saveResult = farmerProfileRepository.saveProfile(profile)
        if (saveResult is Result.Success) {
            sessionRepository.setOnboardingComplete(isComplete = true)
        }
        return saveResult
    }
}

class GetFarmerProfileUseCase @Inject constructor(
    private val farmerProfileRepository: FarmerProfileRepository
) {
    suspend fun execute(userId: String): FarmerProfile? {
        return farmerProfileRepository.getProfile(userId)
    }
}

class GetCurrentFarmerProfileUseCase @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getFarmerProfileUseCase: GetFarmerProfileUseCase
) {
    suspend fun execute(): FarmerProfile? {
        val userId = getCurrentUserIdUseCase.execute() ?: return null
        return getFarmerProfileUseCase.execute(userId)
    }
}

class ObserveFarmerProfileUseCase @Inject constructor(
    private val farmerProfileRepository: FarmerProfileRepository
) {
    fun execute(userId: String) = farmerProfileRepository.observeProfile(userId)
}
