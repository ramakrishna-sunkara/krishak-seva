package com.kisanalert.core.utils

import android.util.Base64
import android.util.Log
import com.kisanalert.domain.model.CropDoctorScan
import java.io.File

data class CropScanImagePayload(
    val photoLink: String,
    val photoBase64: String,
    val photoMimeType: String
)

object CropScanImagePayloadResolver {
    private const val TAG: String = "CropScanImagePayload"
    private const val JPEG_MIME_TYPE: String = "image/jpeg"

    fun resolve(scan: CropDoctorScan): CropScanImagePayload {
        val storageUrl = scan.imageStorageUrl.orEmpty().trim()
        if (storageUrl.isNotEmpty()) {
            return CropScanImagePayload(
                photoLink = storageUrl,
                photoBase64 = "",
                photoMimeType = JPEG_MIME_TYPE
            )
        }
        val imageFile = File(scan.imageLocalPath)
        if (!imageFile.exists()) {
            Log.w(TAG, "Local scan image missing scanId=${scan.id} path=${scan.imageLocalPath}")
            return CropScanImagePayload(
                photoLink = "",
                photoBase64 = "",
                photoMimeType = JPEG_MIME_TYPE
            )
        }
        val compressedBytes = ImageCompressionUtils.compressImageFile(imageFile)
        val photoBase64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        Log.d(
            TAG,
            "Using photoBase64 fallback scanId=${scan.id} bytes=${compressedBytes.size} base64Length=${photoBase64.length}"
        )
        return CropScanImagePayload(
            photoLink = "",
            photoBase64 = photoBase64,
            photoMimeType = JPEG_MIME_TYPE
        )
    }
}
