package com.kisanalert.domain.repository

import android.app.Activity
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.AuthUser
import com.kisanalert.domain.model.PhoneVerificationResult
import com.kisanalert.domain.model.SessionState

interface AuthRepository {
    suspend fun syncSessionWithFirebase(): SessionState
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String
    ): Result<PhoneVerificationResult>
    suspend fun verifyPhoneCode(
        verificationId: String,
        otpCode: String
    ): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): AuthUser?
}
