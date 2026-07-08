package com.kisanalert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_doctor_scans")
data class CropDoctorScanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val cropName: String,
    val imageLocalPath: String,
    val imageStorageUrl: String?,
    val diseaseName: String,
    val confidencePercent: Int,
    val severity: String,
    val symptoms: String,
    val treatmentAdvice: String,
    val preventionTipsJson: String,
    val isHealthy: Boolean,
    val isFromCloud: Boolean,
    val scannedAt: Long
)
