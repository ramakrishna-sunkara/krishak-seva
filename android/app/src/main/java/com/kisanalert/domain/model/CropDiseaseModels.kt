package com.kisanalert.domain.model

enum class DiseaseSeverity(val displayName: String) {
    LOW(displayName = "Low"),
    MEDIUM(displayName = "Medium"),
    HIGH(displayName = "High")
}

data class CropDiseaseDiagnosis(
    val diseaseName: String,
    val confidencePercent: Int,
    val severity: DiseaseSeverity,
    val symptoms: String,
    val treatmentAdvice: String,
    val preventionTips: List<String>,
    val isHealthy: Boolean,
    val isFromCloud: Boolean = true
) {
    fun isLowConfidence(thresholdPercent: Int): Boolean {
        return confidencePercent < thresholdPercent
    }
}

data class CropDoctorScan(
    val id: String,
    val userId: String,
    val cropName: String,
    val imageLocalPath: String,
    val imageStorageUrl: String?,
    val diagnosis: CropDiseaseDiagnosis,
    val scannedAt: Long
)

data class CropDiseaseDetectionRequest(
    val userId: String,
    val scanId: String,
    val cropName: String,
    val farmerName: String,
    val village: String,
    val district: String,
    val state: String,
    val imageBase64: String,
    val imageMimeType: String = "image/jpeg",
    val languageCode: String = PreferredLanguage.TELUGU.code
)

data class CropDiseaseDetectionResult(
    val diagnosis: CropDiseaseDiagnosis,
    val imageStorageUrl: String?
)
