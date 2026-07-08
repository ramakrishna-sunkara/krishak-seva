package com.kisanalert.data.repository

import android.app.Activity
import com.kisanalert.core.utils.Result
import com.kisanalert.data.remote.firebase.FirebaseAuthDataSource
import com.kisanalert.domain.model.AuthUser
import com.kisanalert.domain.model.PhoneVerificationResult
import com.kisanalert.domain.model.SessionState
import com.kisanalert.domain.repository.AuthRepository
import com.kisanalert.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val sessionRepository: SessionRepository
) : AuthRepository {
    override suspend fun syncSessionWithFirebase(): SessionState {
        val currentUser = firebaseAuthDataSource.getCurrentUser()
        if (currentUser != null) {
            sessionRepository.setAuthenticated(
                isAuthenticated = true,
                userId = currentUser.userId
            )
        } else {
            sessionRepository.setAuthenticated(
                isAuthenticated = false,
                userId = null
            )
        }
        return sessionRepository.getSessionState()
    }

    override suspend fun signInAnonymously(): Result<AuthUser> {
        val result = firebaseAuthDataSource.signInAnonymously()
        if (result is Result.Success) {
            persistAuthenticatedUser(result.data)
        }
        return result
    }

    override suspend fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String
    ): Result<PhoneVerificationResult> {
        val formattedPhoneNumber = formatIndianPhoneNumber(phoneNumber)
        if (formattedPhoneNumber == null) {
            return Result.Error(message = "Enter a valid 10-digit mobile number.")
        }
        val result = firebaseAuthDataSource.sendPhoneVerificationCode(
            activity = activity,
            phoneNumber = formattedPhoneNumber
        )
        if (result is Result.Success && result.data is PhoneVerificationResult.AutoVerified) {
            persistAuthenticatedUser(result.data.authUser)
        }
        return result
    }

    override suspend fun verifyPhoneCode(
        verificationId: String,
        otpCode: String
    ): Result<AuthUser> {
        val result = firebaseAuthDataSource.verifyPhoneCode(
            verificationId = verificationId,
            otpCode = otpCode
        )
        if (result is Result.Success) {
            persistAuthenticatedUser(result.data)
        }
        return result
    }

    override suspend fun signOut(): Result<Unit> {
        val result = firebaseAuthDataSource.signOut()
        if (result is Result.Success) {
            sessionRepository.clearSession()
        }
        return result
    }

    override fun getCurrentUser(): AuthUser? {
        return firebaseAuthDataSource.getCurrentUser()
    }

    private suspend fun persistAuthenticatedUser(authUser: AuthUser) {
        sessionRepository.setAuthenticated(
            isAuthenticated = true,
            userId = authUser.userId
        )
    }

    private fun formatIndianPhoneNumber(phoneNumber: String): String? {
        val digitsOnly = phoneNumber.filter { character -> character.isDigit() }
        val normalizedNumber = when {
            digitsOnly.length == 10 -> digitsOnly
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.drop(2)
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> digitsOnly.drop(1)
            else -> return null
        }
        return if (normalizedNumber.length == 10) {
            "+91$normalizedNumber"
        } else {
            null
        }
    }
}
