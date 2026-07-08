package com.kisanalert.data.repository

import com.kisanalert.core.utils.LocalCropRecommendationAdvisor
import com.kisanalert.core.utils.Result
import com.kisanalert.data.local.dao.CropRecommendationDao
import com.kisanalert.data.local.dao.WeatherCacheDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.remote.firebase.CloudFunctionsDataSource
import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.domain.model.CropRecommendationRequest
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.FarmingSeason
import com.kisanalert.domain.repository.CropRecommendationRepository
import com.kisanalert.domain.repository.FarmerProfileRepository
import com.kisanalert.domain.repository.AppSettingsRepository
import com.kisanalert.domain.repository.GroundWaterAssessmentRepository
import com.kisanalert.core.locale.LocaleManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropRecommendationRepositoryImpl @Inject constructor(
    private val cropRecommendationDao: CropRecommendationDao,
    private val farmerProfileRepository: FarmerProfileRepository,
    private val weatherCacheDao: WeatherCacheDao,
    private val cloudFunctionsDataSource: CloudFunctionsDataSource,
    private val appSettingsRepository: AppSettingsRepository,
    private val groundWaterAssessmentRepository: GroundWaterAssessmentRepository
) : CropRecommendationRepository {
    override fun observeRecommendations(
        userId: String,
        season: FarmingSeason
    ): Flow<List<CropRecommendation>> {
        return cropRecommendationDao.observeRecommendations(userId, season.name).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getRecommendations(
        userId: String,
        season: FarmingSeason
    ): List<CropRecommendation> {
        return cropRecommendationDao.getRecommendations(userId, season.name).map { entity ->
            entity.toDomain()
        }
    }

    override suspend fun generateRecommendations(
        userId: String,
        season: FarmingSeason,
        forceRefresh: Boolean
    ): Result<List<CropRecommendation>> {
        val profile = farmerProfileRepository.getProfile(userId)
            ?: return Result.Error(message = "Complete farmer registration to get crop recommendations.")
        if (!forceRefresh) {
            val cachedRecommendations = getRecommendations(userId, season)
            if (cachedRecommendations.isNotEmpty()) {
                return Result.Success(cachedRecommendations)
            }
        }
        val recommendations = fetchRecommendationsWithFallback(
            profile = profile,
            season = season
        )
        cropRecommendationDao.deleteRecommendationsForSeason(userId, season.name)
        cropRecommendationDao.insertRecommendations(
            recommendations.map { recommendation ->
                recommendation.toEntity(userId = userId, season = season)
            }
        )
        return Result.Success(recommendations)
    }

    private suspend fun fetchRecommendationsWithFallback(
        profile: FarmerProfile,
        season: FarmingSeason
    ): List<CropRecommendation> {
        val locationKey = "${profile.district}_${profile.state}".lowercase()
        val weather = weatherCacheDao.getWeather(locationKey)
        val settings = appSettingsRepository.getSettings()
        val languageCode = LocaleManager.resolveLocaleCode(
            profileLanguageCode = profile.preferredLanguage.code,
            storedLocaleCode = settings.localeCode
        )
        val groundWaterAssessment = groundWaterAssessmentRepository.getDistrictAssessment(
            state = profile.state,
            district = profile.district
        )
        val request = CropRecommendationRequest(
            userId = profile.userId,
            farmerName = profile.name,
            village = profile.village,
            district = profile.district,
            state = profile.state,
            farmSizeAcres = profile.farmSizeAcres,
            soilType = profile.soilType.name,
            waterSource = profile.waterSource.name,
            currentCrop = profile.currentCrop,
            season = season.name,
            temperatureCelsius = weather?.temperatureCelsius,
            humidityPercent = weather?.humidityPercent,
            rainVolumeMm = weather?.rainVolumeMm,
            languageCode = languageCode,
            groundWaterCategory = groundWaterAssessment?.category?.name,
            groundWaterStagePercent = groundWaterAssessment?.stageOfExtractionPercent,
            groundWaterAssessmentYear = groundWaterAssessment?.assessmentYear
        )
        return try {
            val cloudRecommendations = cloudFunctionsDataSource.fetchCropRecommendations(request)
            if (cloudRecommendations.isNotEmpty()) {
                cloudRecommendations
            } else {
                LocalCropRecommendationAdvisor.generateRecommendations(
                    profile = profile,
                    season = season,
                    groundWaterAssessment = groundWaterAssessment
                )
            }
        } catch (exception: Exception) {
            LocalCropRecommendationAdvisor.generateRecommendations(
                profile = profile,
                season = season,
                groundWaterAssessment = groundWaterAssessment
            )
        }
    }
}
