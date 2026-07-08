package com.kisanalert.core.utils

import com.kisanalert.core.constants.CropDoctorErrors

object AiServiceErrorMapper {
    fun resolveErrorCode(exception: Throwable?, fallbackMessage: String? = null): String {
        val mappedCode = CropDoctorErrorMapper.resolveErrorCode(exception)
        if (mappedCode != CropDoctorErrors.GENERIC) {
            return mappedCode
        }
        if (CropDoctorErrors.isKnownCode(fallbackMessage)) {
            return fallbackMessage!!
        }
        return mappedCode
    }

    fun isRetryable(errorCode: String): Boolean {
        return CropDoctorErrors.isRetryable(errorCode)
    }
}
