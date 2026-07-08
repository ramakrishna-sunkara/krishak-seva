package com.kisanalert.data.mapper

import com.kisanalert.data.local.entity.CropDoctorScanEntity
import com.kisanalert.domain.model.CropDiseaseDiagnosis
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.domain.model.DiseaseSeverity

private const val PREVENTION_TIPS_DELIMITER: String = "|||"

fun CropDoctorScan.toEntity(): CropDoctorScanEntity {
    return CropDoctorScanEntity(
        id = id,
        userId = userId,
        cropName = cropName,
        imageLocalPath = imageLocalPath,
        imageStorageUrl = imageStorageUrl,
        diseaseName = diagnosis.diseaseName,
        confidencePercent = diagnosis.confidencePercent,
        severity = diagnosis.severity.name,
        symptoms = diagnosis.symptoms,
        treatmentAdvice = diagnosis.treatmentAdvice,
        preventionTipsJson = diagnosis.preventionTips.joinToString(PREVENTION_TIPS_DELIMITER),
        isHealthy = diagnosis.isHealthy,
        isFromCloud = diagnosis.isFromCloud,
        scannedAt = scannedAt
    )
}

fun CropDoctorScanEntity.toDomain(): CropDoctorScan {
    val preventionTips = if (preventionTipsJson.isBlank()) {
        emptyList()
    } else {
        preventionTipsJson.split(PREVENTION_TIPS_DELIMITER).filter { tip -> tip.isNotBlank() }
    }
    return CropDoctorScan(
        id = id,
        userId = userId,
        cropName = cropName,
        imageLocalPath = imageLocalPath,
        imageStorageUrl = imageStorageUrl,
        diagnosis = CropDiseaseDiagnosis(
            diseaseName = diseaseName,
            confidencePercent = confidencePercent,
            severity = DiseaseSeverity.valueOf(severity),
            symptoms = symptoms,
            treatmentAdvice = treatmentAdvice,
            preventionTips = preventionTips,
            isHealthy = isHealthy,
            isFromCloud = isFromCloud
        ),
        scannedAt = scannedAt
    )
}
