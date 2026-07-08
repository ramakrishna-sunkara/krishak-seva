package com.kisanalert.data.remote

import android.util.Log
import com.kisanalert.core.constants.CropDoctorConstants
import com.kisanalert.core.utils.CropScanImagePayloadResolver
import com.kisanalert.core.utils.LowConfidenceAlertFormatter
import com.kisanalert.data.remote.dto.LowConfidenceAlertRequest
import com.kisanalert.domain.model.AuthUser
import com.kisanalert.domain.model.CropDoctorScan
import com.kisanalert.domain.model.FarmerProfile

object LowConfidenceAlertRequestMapper {
    private const val TAG: String = "LowConfidenceAlert"

    fun map(
        scan: CropDoctorScan,
        profile: FarmerProfile,
        authUser: AuthUser?,
        appVersion: String
    ): LowConfidenceAlertRequest {
        val diagnosis = scan.diagnosis
        val photoPayload = CropScanImagePayloadResolver.resolve(scan = scan)
        Log.d(
            TAG,
            "Mapping alert scanId=${scan.id} photoLink=${photoPayload.photoLink.ifBlank { "<empty>" }} " +
                "photoBase64Length=${photoPayload.photoBase64.length}"
        )
        val request = LowConfidenceAlertRequest(
            farmerInfo = LowConfidenceAlertFormatter.formatFarmerInfo(
                farmerName = profile.name,
                phoneNumber = authUser?.phoneNumber
            ),
            photoLink = photoPayload.photoLink,
            photoBase64 = photoPayload.photoBase64,
            photoMimeType = photoPayload.photoMimeType,
            aiGuess = diagnosis.diseaseName,
            confidence = LowConfidenceAlertFormatter.formatConfidence(
                confidencePercent = diagnosis.confidencePercent
            ),
            scanId = scan.id,
            userId = scan.userId,
            farmerName = profile.name,
            phoneNumber = LowConfidenceAlertFormatter.formatPhoneNumber(authUser?.phoneNumber),
            village = profile.village,
            district = profile.district,
            state = profile.state,
            pincode = profile.pincode,
            cropName = scan.cropName.ifBlank { profile.currentCrop },
            farmSizeAcres = LowConfidenceAlertFormatter.formatFarmSize(profile.farmSizeAcres),
            soilType = profile.soilType.name,
            waterSource = profile.waterSource.name,
            preferredLanguage = profile.preferredLanguage.code,
            currentCrop = profile.currentCrop,
            latitude = LowConfidenceAlertFormatter.formatCoordinate(profile.latitude),
            longitude = LowConfidenceAlertFormatter.formatCoordinate(profile.longitude),
            confidencePercent = diagnosis.confidencePercent,
            lowConfidenceThresholdPercent = CropDoctorConstants.LOW_CONFIDENCE_THRESHOLD_PERCENT,
            severity = diagnosis.severity.name,
            isHealthy = diagnosis.isHealthy,
            symptoms = diagnosis.symptoms,
            treatmentAdvice = diagnosis.treatmentAdvice,
            preventionTips = LowConfidenceAlertFormatter.formatPreventionTips(diagnosis.preventionTips),
            imageLocalPath = scan.imageLocalPath,
            scannedAt = LowConfidenceAlertFormatter.formatScannedAt(scan.scannedAt),
            isFromCloud = diagnosis.isFromCloud,
            isAnonymousUser = authUser?.isAnonymous ?: true,
            alertType = CropDoctorConstants.LOW_CONFIDENCE_ALERT_TYPE,
            source = CropDoctorConstants.LOW_CONFIDENCE_ALERT_SOURCE,
            appVersion = appVersion
        )
        Log.d(
            TAG,
            "Mapped low-confidence alert scanId=${request.scanId} " +
                "confidence=${request.confidencePercent}% crop=${request.cropName} " +
                "photoLink=${request.photoLink.ifBlank { "<empty>" }} " +
                "photoBase64Length=${request.photoBase64.length}"
        )
        return request
    }
}
