package com.kisanalert.core.utils

import com.google.firebase.functions.FirebaseFunctionsException
import com.kisanalert.core.constants.CropDoctorErrors

object CropDoctorErrorMapper {
    fun resolveErrorCode(exception: Throwable?): String {
        if (exception == null) {
            return CropDoctorErrors.GENERIC
        }
        val messageChain = buildMessageChain(exception)
        if (CropDoctorErrors.isKnownCode(exception.message)) {
            return exception.message!!
        }
        if (messageChain.contains(CropDoctorErrors.INVALID_CROP_IMAGE)) {
            return CropDoctorErrors.INVALID_CROP_IMAGE
        }
        val functionsException = findFunctionsException(exception)
        when (functionsException?.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                return CropDoctorErrors.UNAUTHENTICATED
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                return CropDoctorErrors.SERVICE_UNAVAILABLE
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                return CropDoctorErrors.TIMEOUT
            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED ->
                return CropDoctorErrors.QUOTA_EXCEEDED
            FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                return CropDoctorErrors.NOT_CONFIGURED
            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                return CropDoctorErrors.NO_IMAGE
            else -> Unit
        }
        if (CropDoctorErrors.isKnownCode(functionsException?.message)) {
            return functionsException?.message!!
        }
        return classifyMessage(messageChain)
    }

    private fun findFunctionsException(exception: Throwable): FirebaseFunctionsException? {
        var current: Throwable? = exception
        while (current != null) {
            if (current is FirebaseFunctionsException) {
                return current
            }
            current = current.cause
        }
        return null
    }

    private fun buildMessageChain(exception: Throwable): String {
        val messages = mutableListOf<String>()
        var current: Throwable? = exception
        while (current != null) {
            val message = current.message
            if (!message.isNullOrBlank()) {
                messages.add(message)
            }
            current = current.cause
        }
        return messages.joinToString(separator = " ")
    }

    private fun classifyMessage(message: String): String {
        val normalizedMessage = message.lowercase()
        return when {
            normalizedMessage.contains("invalid_crop_image") ->
                CropDoctorErrors.INVALID_CROP_IMAGE
            normalizedMessage.contains("quota_exceeded") ||
                normalizedMessage.contains("too many requests") ||
                normalizedMessage.contains("429") ||
                normalizedMessage.contains("quota") ||
                normalizedMessage.contains("rate limit") ->
                CropDoctorErrors.QUOTA_EXCEEDED
            normalizedMessage.contains("deadline_exceeded") ||
                normalizedMessage.contains("timed out") ||
                normalizedMessage.contains("timeout") ->
                CropDoctorErrors.TIMEOUT
            normalizedMessage.contains("unavailable") ||
                normalizedMessage.contains("network") ||
                normalizedMessage.contains("internet") ||
                normalizedMessage.contains("connection") ->
                CropDoctorErrors.SERVICE_UNAVAILABLE
            normalizedMessage.contains("unauthenticated") ||
                normalizedMessage.contains("authentication required") ->
                CropDoctorErrors.UNAUTHENTICATED
            normalizedMessage.contains("api key is not configured") ||
                normalizedMessage.contains("not configured") ->
                CropDoctorErrors.NOT_CONFIGURED
            normalizedMessage.contains("image is required") ||
                normalizedMessage.contains("captured image not found") ->
                CropDoctorErrors.NO_IMAGE
            else -> CropDoctorErrors.GENERIC
        }
    }
}
