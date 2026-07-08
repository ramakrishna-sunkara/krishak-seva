package com.kisanalert.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LowConfidenceAlertRequest(
    val farmerInfo: String,
    val photoLink: String,
    val photoBase64: String,
    val photoMimeType: String,
    val aiGuess: String,
    val confidence: String,
    val scanId: String,
    val userId: String,
    val farmerName: String,
    val phoneNumber: String,
    val village: String,
    val district: String,
    val state: String,
    val pincode: String,
    val cropName: String,
    val farmSizeAcres: String,
    val soilType: String,
    val waterSource: String,
    val preferredLanguage: String,
    val currentCrop: String,
    val latitude: String,
    val longitude: String,
    val confidencePercent: Int,
    val lowConfidenceThresholdPercent: Int,
    val severity: String,
    val isHealthy: Boolean,
    val symptoms: String,
    val treatmentAdvice: String,
    val preventionTips: String,
    val imageLocalPath: String,
    val scannedAt: String,
    val isFromCloud: Boolean,
    val isAnonymousUser: Boolean,
    val alertType: String,
    val source: String,
    val appVersion: String
)
