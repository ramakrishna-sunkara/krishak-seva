package com.kisanalert.core.constants

object CropDoctorErrors {
    const val INVALID_CROP_IMAGE: String = "INVALID_CROP_IMAGE"
    const val QUOTA_EXCEEDED: String = "QUOTA_EXCEEDED"
    const val SERVICE_UNAVAILABLE: String = "SERVICE_UNAVAILABLE"
    const val TIMEOUT: String = "TIMEOUT"
    const val UNAUTHENTICATED: String = "UNAUTHENTICATED"
    const val NOT_CONFIGURED: String = "NOT_CONFIGURED"
    const val NO_IMAGE: String = "NO_IMAGE"
    const val IMAGE_NOT_FOUND: String = "IMAGE_NOT_FOUND"
    const val NO_CAPTURE: String = "NO_CAPTURE"
    const val GENERIC: String = "GENERIC"
    private val KNOWN_CODES: Set<String> = setOf(
        INVALID_CROP_IMAGE,
        QUOTA_EXCEEDED,
        SERVICE_UNAVAILABLE,
        TIMEOUT,
        UNAUTHENTICATED,
        NOT_CONFIGURED,
        NO_IMAGE,
        IMAGE_NOT_FOUND,
        NO_CAPTURE,
        GENERIC
    )
    fun isKnownCode(value: String?): Boolean {
        return value != null && value in KNOWN_CODES
    }
    fun shouldShowExpertContact(errorCode: String): Boolean {
        return errorCode in setOf(
            QUOTA_EXCEEDED,
            SERVICE_UNAVAILABLE,
            TIMEOUT,
            NOT_CONFIGURED,
            GENERIC
        )
    }
    fun isRetryable(errorCode: String): Boolean {
        return errorCode in setOf(
            QUOTA_EXCEEDED,
            SERVICE_UNAVAILABLE,
            TIMEOUT,
            NOT_CONFIGURED,
            GENERIC
        )
    }
    fun shouldShowInlineCard(errorCode: String): Boolean {
        return errorCode == INVALID_CROP_IMAGE || shouldShowExpertContact(errorCode)
    }
}
