package com.kisanalert.core.utils

import com.google.firebase.auth.FirebaseAuthException

object AuthErrorMapper {
    fun mapAuthErrorMessage(exception: Throwable?): String {
        if (exception == null) {
            return "Authentication failed. Please try again."
        }
        val errorCode = (exception as? FirebaseAuthException)?.errorCode
        val message = exception.localizedMessage.orEmpty()
        return when {
            errorCode == "ERROR_OPERATION_NOT_ALLOWED" ||
                message.contains("sign-in provider is disabled", ignoreCase = true) -> {
                "Phone sign-in is disabled. Enable Phone in Firebase Console → Authentication → Sign-in method."
            }
            errorCode == "ERROR_ADMIN_RESTRICTED_OPERATION" ||
                message.contains("restricted to administrators", ignoreCase = true) -> {
                "Guest sign-in is disabled. Enable Anonymous in Firebase Console → Authentication → Sign-in method."
            }
            errorCode == "ERROR_CONFIGURATION_NOT_FOUND" ||
                message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) -> {
                "Phone OTP is not configured. Add SHA-1 & SHA-256 in Firebase Console, " +
                    "enable Phone sign-in, then re-download google-services.json."
            }
            errorCode == "ERROR_INVALID_PHONE_NUMBER" -> {
                "Invalid phone number. Enter a valid 10-digit Indian mobile number."
            }
            errorCode == "ERROR_TOO_MANY_REQUESTS" -> {
                "Too many OTP requests. Please wait a few minutes and try again."
            }
            errorCode == "ERROR_QUOTA_EXCEEDED" -> {
                "SMS quota exceeded. Try again later or use Continue as Guest."
            }
            errorCode == "ERROR_INVALID_VERIFICATION_CODE" -> {
                "Invalid OTP. Please check the code and try again."
            }
            errorCode == "ERROR_SESSION_EXPIRED" -> {
                "OTP session expired. Please request a new OTP."
            }
            message.isNotBlank() -> message
            else -> "Authentication failed. Please try again."
        }
    }
}
