package com.kisanalert.data.remote.firebase

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.kisanalert.core.constants.AppConstants
import com.kisanalert.core.constants.CropDoctorErrors
import com.kisanalert.core.utils.CropDiseaseDiagnosisValidator
import com.kisanalert.core.utils.CropDoctorErrorMapper
import com.kisanalert.core.constants.CloudFunctionEndpoints
import com.kisanalert.domain.model.CropDiseaseDetectionRequest
import com.kisanalert.domain.model.CropDiseaseDetectionResult
import com.kisanalert.domain.model.CropDiseaseDiagnosis
import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.CropRecommendationRequest
import com.kisanalert.domain.model.DiseaseSeverity
import com.kisanalert.domain.model.FarmingQuestionAnswer
import com.kisanalert.domain.model.FarmingQuestionRequest
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.WeatherAdvisoryInsight
import com.kisanalert.domain.model.WeatherForecastDay
import com.kisanalert.domain.model.WeatherSummary
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudFunctionsDataSource @Inject constructor(
    private val firebaseFunctions: FirebaseFunctions
) {
    suspend fun fetchCropRecommendations(
        request: CropRecommendationRequest
    ): List<CropRecommendation> {
        return withTimeout(AppConstants.CLOUD_FUNCTION_TIMEOUT_MS) {
            val payload = mapOf(
                "userId" to request.userId,
                "farmerName" to request.farmerName,
                "village" to request.village,
                "district" to request.district,
                "state" to request.state,
                "farmSizeAcres" to request.farmSizeAcres,
                "soilType" to request.soilType,
                "waterSource" to request.waterSource,
                "currentCrop" to request.currentCrop,
                "season" to request.season,
                "temperatureCelsius" to request.temperatureCelsius,
                "humidityPercent" to request.humidityPercent,
                "rainVolumeMm" to request.rainVolumeMm,
                "languageCode" to request.languageCode,
                "groundWaterCategory" to request.groundWaterCategory,
                "groundWaterStagePercent" to request.groundWaterStagePercent,
                "groundWaterAssessmentYear" to request.groundWaterAssessmentYear
            )
            val result = firebaseFunctions
                .getHttpsCallable(CloudFunctionEndpoints.CROP_RECOMMENDATION)
                .call(payload)
                .await()
            parseRecommendations(result.getData())
        }
    }

    suspend fun fetchWeatherAdvisory(
        profile: FarmerProfile,
        weather: WeatherSummary,
        forecastDays: List<WeatherForecastDay>,
        languageCode: String
    ): WeatherAdvisoryInsight {
        return withTimeout(AppConstants.CLOUD_FUNCTION_TIMEOUT_MS) {
            val forecastPayload = forecastDays.map { day ->
                mapOf(
                    "dayLabel" to day.dayLabel,
                    "temperatureCelsius" to day.temperatureCelsius,
                    "description" to day.description,
                    "humidityPercent" to day.humidityPercent,
                    "rainVolumeMm" to day.rainVolumeMm
                )
            }
            val payload = mapOf(
                "userId" to profile.userId,
                "farmerName" to profile.name,
                "village" to profile.village,
                "district" to profile.district,
                "state" to profile.state,
                "currentCrop" to profile.currentCrop,
                "waterSource" to profile.waterSource.name,
                "soilType" to profile.soilType.name,
                "temperatureCelsius" to weather.temperatureCelsius,
                "humidityPercent" to weather.humidityPercent,
                "rainVolumeMm" to weather.rainVolumeMm,
                "windSpeedKmh" to weather.windSpeedKmh,
                "weatherDescription" to weather.description,
                "forecastDays" to forecastPayload,
                "languageCode" to languageCode
            )
            val result = firebaseFunctions
                .getHttpsCallable(CloudFunctionEndpoints.WEATHER_ADVISORY)
                .call(payload)
                .await()
            parseWeatherAdvisory(result.getData())
        }
    }

    suspend fun detectCropDisease(request: CropDiseaseDetectionRequest): CropDiseaseDetectionResult {
        return try {
            withTimeout(AppConstants.CLOUD_FUNCTION_TIMEOUT_MS) {
                Log.d(TAG, "detectCropDisease languageCode=${request.languageCode} scanId=${request.scanId}")
                val payload = mapOf(
                    "userId" to request.userId,
                    "scanId" to request.scanId,
                    "cropName" to request.cropName,
                    "farmerName" to request.farmerName,
                    "village" to request.village,
                    "district" to request.district,
                    "state" to request.state,
                    "imageBase64" to request.imageBase64,
                    "imageMimeType" to request.imageMimeType,
                    "languageCode" to request.languageCode
                )
                val result = firebaseFunctions
                    .getHttpsCallable(CloudFunctionEndpoints.DISEASE_DETECTION)
                    .call(payload)
                    .await()
                parseCropDiseaseDetectionResult(
                    data = result.getData(),
                    languageCode = request.languageCode
                )
            }
        } catch (exception: Exception) {
            throw IllegalStateException(mapCropDoctorError(exception), exception)
        }
    }

    suspend fun uploadCropScanImage(
        scanId: String,
        imageBase64: String,
        imageMimeType: String
    ): String? {
        return try {
            withTimeout(AppConstants.STORAGE_UPLOAD_TIMEOUT_MS) {
                val payload = mapOf(
                    "scanId" to scanId,
                    "imageBase64" to imageBase64,
                    "imageMimeType" to imageMimeType
                )
                val result = firebaseFunctions
                    .getHttpsCallable(CloudFunctionEndpoints.UPLOAD_CROP_SCAN_IMAGE)
                    .call(payload)
                    .await()
                val responseMap = result.getData() as? Map<*, *>
                responseMap?.get("imageStorageUrl") as? String
            }
        } catch (exception: Exception) {
            Log.w(
                "CloudFunctions",
                "uploadCropScanImage failed scanId=$scanId: ${exception.message}",
                exception
            )
            null
        }
    }

    suspend fun askFarmingQuestion(request: FarmingQuestionRequest): FarmingQuestionAnswer {
        return withTimeout(AppConstants.CLOUD_FUNCTION_TIMEOUT_MS) {
            val payload = mapOf(
                "userId" to request.userId,
                "question" to request.question,
                "languageCode" to request.languageCode,
                "farmerName" to request.farmerName,
                "village" to request.village,
                "district" to request.district,
                "state" to request.state,
                "currentCrop" to request.currentCrop,
                "soilType" to request.soilType,
                "waterSource" to request.waterSource
            )
            val result = firebaseFunctions
                .getHttpsCallable(CloudFunctionEndpoints.VOICE_ASSISTANT)
                .call(payload)
                .await()
            parseFarmingQuestionAnswer(
                data = result.getData(),
                languageCode = request.languageCode
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFarmingQuestionAnswer(
        data: Any?,
        languageCode: String
    ): FarmingQuestionAnswer {
        val responseMap = data as? Map<String, Any?> ?: throw IllegalStateException("Invalid voice response.")
        val answer = responseMap["answer"] as? String ?: throw IllegalStateException("Missing answer.")
        val followUpRaw = responseMap["followUpSuggestions"] as? List<*>
            ?: responseMap["suggestions"] as? List<*>
        val followUpSuggestions = followUpRaw?.mapNotNull { item -> item as? String } ?: emptyList()
        val isTelugu = languageCode == PreferredLanguage.TELUGU.code
        return FarmingQuestionAnswer(
            answer = answer,
            followUpSuggestions = followUpSuggestions.ifEmpty {
                if (isTelugu) {
                    listOf(
                        "నీటి పారుదల ఎప్పుడు చేయాలి?",
                        "ఎరువు ఏమి వేయాలి?",
                        "పురుగులను ఎలా నియంత్రించాలి?"
                    )
                } else {
                    listOf(
                        "When should I irrigate?",
                        "What fertilizer should I use?",
                        "How do I control pests?"
                    )
                }
            },
            isFromCloud = true
        )
    }

    private fun parseCropDiseaseDetectionResult(
        data: Any?,
        languageCode: String
    ): CropDiseaseDetectionResult {
        val responseMap = data as? Map<String, Any?> ?: throw IllegalStateException("Invalid diagnosis response.")
        val imageStorageUrl = responseMap["imageStorageUrl"] as? String
        return CropDiseaseDetectionResult(
            diagnosis = parseCropDiseaseDiagnosis(
                data = responseMap,
                languageCode = languageCode
            ),
            imageStorageUrl = imageStorageUrl?.takeIf { url -> url.isNotBlank() }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseCropDiseaseDiagnosis(
        data: Any?,
        languageCode: String
    ): CropDiseaseDiagnosis {
        val isTelugu = languageCode == PreferredLanguage.TELUGU.code
        val responseMap = data as? Map<String, Any?> ?: throw IllegalStateException("Invalid diagnosis response.")
        val diseaseName = responseMap["diseaseName"] as? String
            ?: if (isTelugu) "తెలியని పరిస్థితి" else "Unknown Condition"
        val confidencePercent = (responseMap["confidencePercent"] as? Number)?.toInt()
            ?: (responseMap["confidence"] as? Number)?.toInt()
            ?: 50
        val severityRaw = responseMap["severity"] as? String ?: "MEDIUM"
        val symptoms = responseMap["symptoms"] as? String
            ?: if (isTelugu) "లక్షణాలు నిర్ణయించలేకపోయాము." else "Symptoms could not be determined."
        val treatmentAdvice = responseMap["treatmentAdvice"] as? String
            ?: responseMap["treatment"] as? String
            ?: if (isTelugu) {
                "మీ స్థానిక వ్యవసాయ అధికారిని సంప్రదించండి."
            } else {
                "Consult your local agricultural officer."
            }
        val preventionTipsRaw = responseMap["preventionTips"] as? List<*>
            ?: responseMap["prevention_tips"] as? List<*>
        val preventionTips = preventionTipsRaw?.mapNotNull { tip -> tip as? String } ?: emptyList()
        val isHealthy = responseMap["isHealthy"] as? Boolean ?: false
        val isValidCropImageFlag = responseMap["isValidCropImage"] as? Boolean
        val isFromCloud = responseMap["isFromCloud"] as? Boolean ?: true
        if (!isFromCloud) {
            throw IllegalStateException(CropDoctorErrors.SERVICE_UNAVAILABLE)
        }
        val severity = parseSeverity(severityRaw)
        val diagnosis = CropDiseaseDiagnosis(
            diseaseName = diseaseName,
            confidencePercent = confidencePercent.coerceIn(0, 100),
            severity = severity,
            symptoms = symptoms,
            treatmentAdvice = treatmentAdvice,
            preventionTips = preventionTips.ifEmpty {
                if (isTelugu) {
                    listOf("పంట మార్పులను ప్రతిరోజు పర్యవేక్షించండి.")
                } else {
                    listOf("Monitor the crop daily for changes.")
                }
            },
            isHealthy = isHealthy,
            isFromCloud = isFromCloud
        )
        if (!CropDiseaseDiagnosisValidator.isValidCropImage(
                diagnosis = diagnosis,
                isValidCropImageFlag = isValidCropImageFlag
            )
        ) {
            throw IllegalStateException(CropDoctorErrors.INVALID_CROP_IMAGE)
        }
        return diagnosis
    }

    private fun parseSeverity(severityRaw: String): DiseaseSeverity {
        return when (severityRaw.uppercase()) {
            "LOW" -> DiseaseSeverity.LOW
            "HIGH" -> DiseaseSeverity.HIGH
            else -> DiseaseSeverity.MEDIUM
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseWeatherAdvisory(data: Any?): WeatherAdvisoryInsight {
        val responseMap = data as? Map<String, Any?> ?: throw IllegalStateException("Invalid advisory response.")
        val summary = responseMap["summary"] as? String ?: throw IllegalStateException("Missing summary.")
        val irrigationAdvice = responseMap["irrigationAdvice"] as? String
            ?: responseMap["irrigation_advice"] as? String
            ?: "Follow regular irrigation schedule."
        val cropRiskLevel = responseMap["cropRiskLevel"] as? String
            ?: responseMap["cropRisk"] as? String
            ?: "Medium"
        val tomorrowOutlook = responseMap["tomorrowOutlook"] as? String
            ?: responseMap["tomorrow_outlook"] as? String
            ?: "Check forecast again later."
        val alertTipsRaw = responseMap["alertTips"] as? List<*>
            ?: responseMap["alerts"] as? List<*>
        val alertTips = alertTipsRaw?.mapNotNull { tip -> tip as? String } ?: emptyList()
        return WeatherAdvisoryInsight(
            summary = summary,
            irrigationAdvice = irrigationAdvice,
            cropRiskLevel = cropRiskLevel,
            tomorrowOutlook = tomorrowOutlook,
            alertTips = alertTips.ifEmpty { listOf("Monitor field conditions daily.") },
            isFromCloud = true
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRecommendations(data: Any?): List<CropRecommendation> {
        val responseMap = data as? Map<String, Any?> ?: return emptyList()
        val recommendationsRaw = responseMap["recommendations"] as? List<*>
        if (recommendationsRaw != null) {
            return recommendationsRaw.mapNotNull { item ->
                (item as? Map<String, Any?>)?.let { recommendationMap ->
                    parseRecommendation(recommendationMap)
                }
            }
        }
        return listOfNotNull(parseRecommendation(responseMap))
    }

    private fun parseRecommendation(item: Map<String, Any?>): CropRecommendation? {
        val cropName = item["cropName"] as? String ?: return null
        val reason = item["reason"] as? String ?: return null
        val riskScore = (item["riskScore"] as? Number)?.toInt() ?: 50
        val waterRequirement = item["waterRequirement"] as? String ?: "Medium"
        val expectedYield = item["expectedYield"] as? String ?: "N/A"
        val fertilizerAdvice = item["fertilizerAdvice"] as? String ?: "Follow local advisory."
        return CropRecommendation(
            cropName = cropName,
            reason = reason,
            riskScore = riskScore.coerceIn(0, 100),
            waterRequirement = waterRequirement,
            expectedYield = expectedYield,
            fertilizerAdvice = fertilizerAdvice,
            isFromCloud = true
        )
    }

    companion object {
        private const val TAG: String = "CloudFunctions"
    }

    fun mapCropDoctorError(exception: Exception): String {
        return CropDoctorErrorMapper.resolveErrorCode(exception)
    }

    fun mapFunctionsError(exception: Exception): String {
        val functionsException = exception as? FirebaseFunctionsException
        return when (functionsException?.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                "Please sign in again to get AI recommendations."
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                "Cloud service unavailable. Showing offline recommendations."
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                "Recommendation request timed out. Showing offline recommendations."
            else -> exception.localizedMessage ?: "Failed to fetch AI recommendations."
        }
    }
}
