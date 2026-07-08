package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.CropDoctorScan

interface CropDoctorRepository {
    suspend fun getScanHistory(userId: String): List<CropDoctorScan>
    suspend fun analyzeCropImage(
        userId: String,
        imageLocalPath: String,
        cropName: String,
        farmerName: String,
        village: String,
        district: String,
        state: String,
        languageCode: String
    ): Result<CropDoctorScan>
    suspend fun ensureScanImageStorageUrl(scan: CropDoctorScan): CropDoctorScan
}
