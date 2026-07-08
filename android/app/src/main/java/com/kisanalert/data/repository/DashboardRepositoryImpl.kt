package com.kisanalert.data.repository

import com.kisanalert.core.constants.AppConstants
import com.kisanalert.core.utils.DashboardAdvisor
import com.kisanalert.core.utils.Result
import com.kisanalert.data.local.dao.AlertDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.remote.WeatherRemoteDataSource
import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.model.DashboardData
import com.kisanalert.domain.model.DashboardSeedAlerts
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.repository.DashboardRepository
import com.kisanalert.domain.repository.FarmerProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val farmerProfileRepository: FarmerProfileRepository,
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val alertDao: AlertDao
) : DashboardRepository {
    override fun observeDashboard(userId: String): Flow<DashboardData?> {
        return flow {
            emit(buildDashboard(userId = userId, forceRefresh = false))
        }
    }

    override suspend fun refreshDashboard(userId: String): Result<DashboardData> {
        return try {
            val dashboardData = buildDashboard(userId = userId, forceRefresh = true)
            if (dashboardData != null) {
                Result.Success(dashboardData)
            } else {
                Result.Error(message = "Farmer profile not found. Please complete registration.")
            }
        } catch (exception: Exception) {
            val cachedDashboard = buildDashboard(userId = userId, forceRefresh = false)
            if (cachedDashboard != null) {
                Result.Success(cachedDashboard.copy(isWeatherFromCache = true))
            } else {
                Result.Error(
                    message = exception.localizedMessage ?: "Failed to load dashboard.",
                    exception = exception
                )
            }
        }
    }

    private suspend fun buildDashboard(
        userId: String,
        forceRefresh: Boolean
    ): DashboardData? {
        val profile = farmerProfileRepository.getProfile(userId) ?: return null
        ensureSeedAlerts(userId = userId, profile = profile)
        val locationKey = buildLocationKey(profile)
        val weatherResult = weatherRemoteDataSource.loadWeatherData(
            profile = profile,
            locationKey = locationKey,
            forceRefresh = forceRefresh
        )
        val alerts = alertDao.getAlerts(userId = userId).map { entity ->
            entity.toDomain()
        }
        return DashboardData(
            farmerName = profile.name,
            village = profile.village,
            currentCrop = profile.currentCrop,
            weather = weatherResult.summary,
            waterSavingScore = DashboardAdvisor.calculateWaterSavingScore(
                profile = profile,
                weather = weatherResult.summary
            ),
            cropHealthStatus = DashboardAdvisor.calculateCropHealth(
                profile = profile,
                weather = weatherResult.summary
            ),
            irrigationAdvice = DashboardAdvisor.buildIrrigationAdvice(
                profile = profile,
                weather = weatherResult.summary
            ),
            aiRecommendation = DashboardAdvisor.buildAiRecommendation(
                profile = profile,
                weather = weatherResult.summary
            ),
            recentAlerts = alerts,
            isWeatherFromCache = weatherResult.isFromCache
        )
    }

    private suspend fun ensureSeedAlerts(userId: String, profile: FarmerProfile) {
        val alertCount = alertDao.getAlertCount(userId)
        if (alertCount > 0) {
            return
        }
        val currentTime = System.currentTimeMillis()
        val seedAlerts = listOf(
            DashboardAlert(
                id = DashboardSeedAlerts.WELCOME_ID,
                title = "Welcome to ${AppConstants.APP_DISPLAY_NAME}",
                message = "${profile.currentCrop}|${profile.village}",
                type = AlertType.IRRIGATION,
                timestamp = currentTime
            ),
            DashboardAlert(
                id = DashboardSeedAlerts.WEATHER_ID,
                title = "Weather Monitoring Active",
                message = profile.district,
                type = AlertType.WEATHER,
                timestamp = currentTime - 3_600_000L
            ),
            DashboardAlert(
                id = DashboardSeedAlerts.CROP_HEALTH_ID,
                title = "Crop Health Check",
                message = "",
                type = AlertType.DISEASE,
                timestamp = currentTime - 7_200_000L
            )
        )
        alertDao.insertAlerts(seedAlerts.map { alert -> alert.toEntity(userId) })
    }

    private fun buildLocationKey(profile: FarmerProfile): String {
        return "${profile.district}_${profile.state}".lowercase()
    }
}
