package com.kisanalert.data.repository

import android.util.Log
import com.kisanalert.core.constants.CropDoctorErrors
import com.kisanalert.core.utils.ImageCompressionUtils
import com.kisanalert.core.utils.Result
import com.kisanalert.data.local.dao.CropDoctorScanDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.remote.firebase.CloudFunctionsDataSource
import com.kisanalert.data.remote.firebase.FirebaseStorageDataSource
import com.kisanalert.domain.model.CropDiseaseDetectionRequest
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.domain.repository.CropDoctorRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropDoctorRepositoryImpl @Inject constructor(
    private val cropDoctorScanDao: CropDoctorScanDao,
    private val cloudFunctionsDataSource: CloudFunctionsDataSource,
    private val firebaseStorageDataSource: FirebaseStorageDataSource
) : CropDoctorRepository {
    override suspend fun getScanHistory(userId: String): List<CropDoctorScan> {
        return cropDoctorScanDao.getRecentScans(userId).map { entity -> entity.toDomain() }
    }

    override suspend fun analyzeCropImage(
        userId: String,
        imageLocalPath: String,
        cropName: String,
        farmerName: String,
        village: String,
        district: String,
        state: String,
        languageCode: String
    ): Result<CropDoctorScan> {
        val imageFile = File(imageLocalPath)
        if (!imageFile.exists()) {
            return Result.Error(message = CropDoctorErrors.IMAGE_NOT_FOUND)
        }
        return try {
            val scanId = UUID.randomUUID().toString()
            val compressedBytes = ImageCompressionUtils.compressImageFile(imageFile)
            val imageBase64 = encodeImageToBase64(compressedBytes)
            val detectionResult = cloudFunctionsDataSource.detectCropDisease(
                request = CropDiseaseDetectionRequest(
                    userId = userId,
                    scanId = scanId,
                    cropName = cropName,
                    farmerName = farmerName,
                    village = village,
                    district = district,
                    state = state,
                    imageBase64 = imageBase64,
                    languageCode = languageCode
                )
            )
            val storageUrl = detectionResult.imageStorageUrl?.takeIf { url ->
                url.isNotBlank()
            } ?: resolveStorageUrl(
                userId = userId,
                scanId = scanId,
                imageBytes = compressedBytes
            )
            if (storageUrl.isNullOrBlank()) {
                Log.w(TAG, "No imageStorageUrl available for scanId=$scanId")
            } else {
                Log.d(TAG, "Resolved imageStorageUrl for scanId=$scanId url=$storageUrl")
            }
            val scan = CropDoctorScan(
                id = scanId,
                userId = userId,
                cropName = cropName,
                imageLocalPath = imageLocalPath,
                imageStorageUrl = storageUrl,
                diagnosis = detectionResult.diagnosis,
                scannedAt = System.currentTimeMillis()
            )
            cropDoctorScanDao.insertScan(scan.toEntity())
            Result.Success(scan)
        } catch (exception: Exception) {
            Result.Error(
                message = cloudFunctionsDataSource.mapCropDoctorError(exception),
                exception = exception
            )
        }
    }

    override suspend fun ensureScanImageStorageUrl(scan: CropDoctorScan): CropDoctorScan {
        if (!scan.imageStorageUrl.isNullOrBlank()) {
            return scan
        }
        val imageFile = File(scan.imageLocalPath)
        if (!imageFile.exists()) {
            Log.w(TAG, "Cannot upload scan image; local file missing scanId=${scan.id}")
            return scan
        }
        val imageBytes = ImageCompressionUtils.compressImageFile(imageFile)
        val storageUrl = resolveStorageUrl(
            userId = scan.userId,
            scanId = scan.id,
            imageBytes = imageBytes
        )
        if (storageUrl.isNullOrBlank()) {
            Log.w(TAG, "Storage upload failed for scanId=${scan.id}")
            return scan
        }
        val updatedScan = scan.copy(imageStorageUrl = storageUrl)
        cropDoctorScanDao.insertScan(updatedScan.toEntity())
        Log.d(TAG, "Resolved storage URL for scanId=${scan.id} url=$storageUrl")
        return updatedScan
    }

    private suspend fun resolveStorageUrl(
        userId: String,
        scanId: String,
        imageBytes: ByteArray
    ): String? {
        return try {
            firebaseStorageDataSource.uploadCropScanImage(
                userId = userId,
                scanId = scanId,
                imageBytes = imageBytes
            )
        } catch (directException: Exception) {
            Log.w(
                TAG,
                "Direct storage upload failed scanId=$scanId, trying cloud function",
                directException
            )
            try {
                cloudFunctionsDataSource.uploadCropScanImage(
                    scanId = scanId,
                    imageBase64 = encodeImageToBase64(imageBytes),
                    imageMimeType = "image/jpeg"
                )
            } catch (cloudException: Exception) {
                Log.w(TAG, "Cloud upload also failed scanId=$scanId", cloudException)
                null
            }
        }
    }

    private fun encodeImageToBase64(imageBytes: ByteArray): String {
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
    }

    companion object {
        private const val TAG: String = "CropDoctorRepository"
    }
}
