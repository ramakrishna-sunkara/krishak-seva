package com.kisanalert.data.remote.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.kisanalert.core.utils.AuthErrorMapper
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.AuthUser
import com.kisanalert.domain.model.PhoneVerificationResult
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun getCurrentUser(): AuthUser? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return AuthUser(
            userId = firebaseUser.uid,
            phoneNumber = firebaseUser.phoneNumber,
            isAnonymous = firebaseUser.isAnonymous
        )
    }

    suspend fun signInAnonymously(): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user
                ?: return Result.Error(message = "Anonymous sign-in failed. Please try again.")
            Result.Success(
                AuthUser(
                    userId = firebaseUser.uid,
                    phoneNumber = firebaseUser.phoneNumber,
                    isAnonymous = firebaseUser.isAnonymous
                )
            )
        } catch (exception: Exception) {
            Result.Error(
                message = AuthErrorMapper.mapAuthErrorMessage(exception),
                exception = exception
            )
        }
    }

    suspend fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String
    ): Result<PhoneVerificationResult> {
        return suspendCoroutine { continuation ->
            val isResumed = AtomicBoolean(false)
            fun resumeOnce(result: Result<PhoneVerificationResult>) {
                if (isResumed.compareAndSet(false, true)) {
                    continuation.resume(result)
                }
            }
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    firebaseAuth.signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            val firebaseUser = authResult.user
                            if (firebaseUser == null) {
                                resumeOnce(
                                    Result.Error(message = "Phone sign-in failed. Please try again.")
                                )
                                return@addOnSuccessListener
                            }
                            resumeOnce(
                                Result.Success(
                                    PhoneVerificationResult.AutoVerified(
                                        authUser = AuthUser(
                                            userId = firebaseUser.uid,
                                            phoneNumber = firebaseUser.phoneNumber,
                                            isAnonymous = firebaseUser.isAnonymous
                                        )
                                    )
                                )
                            )
                        }
                        .addOnFailureListener { exception ->
                            resumeOnce(
                                Result.Error(
                                    message = AuthErrorMapper.mapAuthErrorMessage(exception),
                                    exception = exception
                                )
                            )
                        }
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    resumeOnce(
                        Result.Error(
                            message = AuthErrorMapper.mapAuthErrorMessage(exception),
                            exception = exception
                        )
                    )
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    resumeOnce(
                        Result.Success(PhoneVerificationResult.CodeSent(verificationId = verificationId))
                    )
                }
            }
            val phoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(PHONE_AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
        }
    }

    suspend fun verifyPhoneCode(
        verificationId: String,
        otpCode: String
    ): Result<AuthUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            signInWithCredential(credential)
        } catch (exception: Exception) {
            Result.Error(
                message = AuthErrorMapper.mapAuthErrorMessage(exception),
                exception = exception
            )
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(
                message = exception.localizedMessage ?: "Sign out failed.",
                exception = exception
            )
        }
    }

    private suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.Error(message = "Phone sign-in failed. Please try again.")
            Result.Success(
                AuthUser(
                    userId = firebaseUser.uid,
                    phoneNumber = firebaseUser.phoneNumber,
                    isAnonymous = firebaseUser.isAnonymous
                )
            )
        } catch (exception: Exception) {
            Result.Error(
                message = AuthErrorMapper.mapAuthErrorMessage(exception),
                exception = exception
            )
        }
    }

    private companion object {
        const val PHONE_AUTH_TIMEOUT_SECONDS: Long = 60L
    }
}
