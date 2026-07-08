package com.kisanalert.data.remote

import android.util.Log
import com.kisanalert.core.constants.CropDoctorConstants
import com.kisanalert.data.remote.dto.LowConfidenceAlertRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LowConfidenceAlertDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    suspend fun submitAlert(request: LowConfidenceAlertRequest) {
        withContext(Dispatchers.IO) {
            try {
                val requestJson = json.encodeToString(
                    LowConfidenceAlertRequest.serializer(),
                    request
                )
                logRequestSummary(request = request, requestJsonLength = requestJson.length)
                Log.d(TAG, "Request JSON: ${buildLogSafeJson(requestJson = requestJson, request = request)}")
                Log.d(TAG, "POST ${CropDoctorConstants.LOW_CONFIDENCE_ALERT_WEB_APP_URL}")
                val requestBody = requestJson.toRequestBody(JSON_MEDIA_TYPE)
                val httpRequest = Request.Builder()
                    .url(CropDoctorConstants.LOW_CONFIDENCE_ALERT_WEB_APP_URL)
                    .post(requestBody)
                    .header("Content-Type", "application/json")
                    .build()
                okHttpClient.newCall(httpRequest).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        Log.d(
                            TAG,
                            "Low-confidence alert success: HTTP ${response.code} body=${responseBody.ifBlank { "<empty>" }}"
                        )
                    } else {
                        Log.w(
                            TAG,
                            "Low-confidence alert failed: HTTP ${response.code} body=${responseBody.ifBlank { "<empty>" }}"
                        )
                    }
                }
            } catch (exception: Exception) {
                Log.w(TAG, "Low-confidence alert failed", exception)
            }
        }
    }

    private fun logRequestSummary(request: LowConfidenceAlertRequest, requestJsonLength: Int) {
        Log.d(
            TAG,
            """
            |Low-confidence alert payload summary:
            |  scanId=${request.scanId}
            |  userId=${request.userId}
            |  farmer=${request.farmerName} (${request.farmerInfo})
            |  phone=${request.phoneNumber}
            |  location=${request.village}, ${request.district}, ${request.state} ${request.pincode}
            |  crop=${request.cropName} currentCrop=${request.currentCrop}
            |  farmSizeAcres=${request.farmSizeAcres} soil=${request.soilType} water=${request.waterSource}
            |  language=${request.preferredLanguage}
            |  coordinates=(${request.latitude}, ${request.longitude})
            |  aiGuess=${request.aiGuess}
            |  confidence=${request.confidence} threshold=${request.lowConfidenceThresholdPercent}%
            |  severity=${request.severity} isHealthy=${request.isHealthy} isFromCloud=${request.isFromCloud}
            |  symptoms=${request.symptoms}
            |  treatmentAdvice=${request.treatmentAdvice}
            |  preventionTips=${request.preventionTips}
            |  photoLink=${request.photoLink.ifBlank { "<empty>" }}
            |  photoBase64Length=${request.photoBase64.length} photoMimeType=${request.photoMimeType}
            |  scannedAt=${request.scannedAt}
            |  alertType=${request.alertType} source=${request.source} appVersion=${request.appVersion}
            |  isAnonymousUser=${request.isAnonymousUser}
            |  requestJsonBytes=$requestJsonLength
            """.trimMargin()
        )
    }

    private fun buildLogSafeJson(
        requestJson: String,
        request: LowConfidenceAlertRequest
    ): String {
        if (request.photoBase64.isEmpty()) {
            return requestJson
        }
        return requestJson.replace(
            oldValue = request.photoBase64,
            newValue = "<photoBase64 omitted length=${request.photoBase64.length}>"
        )
    }

    companion object {
        private const val TAG: String = "LowConfidenceAlert"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
