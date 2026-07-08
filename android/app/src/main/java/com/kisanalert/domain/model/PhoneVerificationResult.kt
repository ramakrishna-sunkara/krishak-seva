package com.kisanalert.domain.model

sealed class PhoneVerificationResult {
    data class CodeSent(val verificationId: String) : PhoneVerificationResult()
    data class AutoVerified(val authUser: AuthUser) : PhoneVerificationResult()
}
