package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.AuthUser
import com.kisanalert.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyPhoneCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(
        verificationId: String,
        otpCode: String
    ): Result<AuthUser> {
        return authRepository.verifyPhoneCode(
            verificationId = verificationId,
            otpCode = otpCode
        )
    }
}
