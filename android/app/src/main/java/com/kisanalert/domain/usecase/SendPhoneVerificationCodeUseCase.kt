package com.kisanalert.domain.usecase

import android.app.Activity
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PhoneVerificationResult
import com.kisanalert.domain.repository.AuthRepository
import javax.inject.Inject

class SendPhoneVerificationCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(
        activity: Activity,
        phoneNumber: String
    ): Result<PhoneVerificationResult> {
        return authRepository.sendPhoneVerificationCode(
            activity = activity,
            phoneNumber = phoneNumber
        )
    }
}
