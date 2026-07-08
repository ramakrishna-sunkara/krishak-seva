package com.kisanalert.data.repository

import com.kisanalert.core.utils.LocalWeatherAdvisoryAdvisor
import com.kisanalert.core.utils.Result
import com.kisanalert.data.local.dao.WeatherAdvisoryCacheDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.remote.WeatherRemoteDataSource
import com.kisanalert.data.remote.firebase.CloudFunctionsDataSource
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.WeatherAdvisoryData
import com.kisanalert.domain.model.WeatherAdvisoryInsight
import com.kisanalert.domain.model.WeatherForecastDay
import com.kisanalert.domain.model.WeatherSummary
import com.kisanalert.domain.repository.FarmerProfileRepository
import com.kisanalert.domain.repository.WeatherAdvisoryRepository
import com.kisanalert.domain.repository.AppSettingsRepository
import com.kisanalert.core.locale.LocaleManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherAdvisoryRepositoryImpl @Inject constructor(
    private val farmerProfileRepository: FarmerProfileRepository,
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherAdvisoryCacheDao: WeatherAdvisoryCacheDao,
    private val cloudFunctionsDataSource: CloudFunctionsDataSource,
    private val appSettingsRepository: AppSettingsRepository
) : WeatherAdvisoryRepository {
    override suspend fun loadWeatherAdvisory(
        userId: String,
        forceRefresh: Boolean
    ): Result<WeatherAdvisoryData> {
        val profile = farmerProfileRepository.getProfile(userId)
            ?: return Result.Error(message = "Complete farmer registration to view weather advisory.")
        return try {
            val locationKey = buildLocationKey(profile)
            val weatherResult = weatherRemoteDataSource.loadWeatherData(
                profile = profile,
                locationKey = locationKey,
                forceRefresh = forceRefresh
            )
            val forecastDays = weatherResult.forecastDays.ifEmpty {
                weatherRemoteDataSource.loadForecastDays(profile = profile)
            }
            val advisory = loadAdvisoryInsight(
                profile = profile,
                locationKey = locationKey,
                weather = weatherResult.summary,
                forecastDays = forecastDays,
                forceRefresh = forceRefresh
            )
            Result.Success(
                WeatherAdvisoryData(
                    locationName = "${profile.village}, ${weatherResult.summary.cityName}",
                    currentCrop = profile.currentCrop,
                    currentWeather = weatherResult.summary,
                    forecastDays = forecastDays,
                    advisory = advisory,
                    isWeatherFromCache = weatherResult.isFromCache
                )
            )
        } catch (exception: Exception) {
            Result.Error(
                message = exception.localizedMessage ?: "Failed to load weather advisory.",
                exception = exception
            )
        }
    }

    private suspend fun loadAdvisoryInsight(
        profile: FarmerProfile,
        locationKey: String,
        weather: WeatherSummary,
        forecastDays: List<WeatherForecastDay>,
        forceRefresh: Boolean
    ): WeatherAdvisoryInsight {
        if (!forceRefresh) {
            val cachedEntity = weatherAdvisoryCacheDao.getAdvisory(locationKey)
            if (cachedEntity != null && isCacheValid(cachedEntity.updatedAt)) {
                return cachedEntity.toDomain()
            }
        }
        val advisory = fetchAdvisoryWithFallback(
            profile = profile,
            weather = weather,
            forecastDays = forecastDays
        )
        weatherAdvisoryCacheDao.insertAdvisory(advisory.toEntity(locationKey))
        return advisory
    }

    private fun isCacheValid(updatedAt: Long): Boolean {
        val cacheAgeMs = System.currentTimeMillis() - updatedAt
        return cacheAgeMs < ADVISORY_CACHE_TTL_MS
    }

    private suspend fun fetchAdvisoryWithFallback(
        profile: FarmerProfile,
        weather: WeatherSummary,
        forecastDays: List<WeatherForecastDay>
    ): WeatherAdvisoryInsight {
        val settings = appSettingsRepository.getSettings()
        val languageCode = LocaleManager.resolveLocaleCode(
            profileLanguageCode = profile.preferredLanguage.code,
            storedLocaleCode = settings.localeCode
        )
        return try {
            cloudFunctionsDataSource.fetchWeatherAdvisory(
                profile = profile,
                weather = weather,
                forecastDays = forecastDays,
                languageCode = languageCode
            )
        } catch (exception: Exception) {
            LocalWeatherAdvisoryAdvisor.buildAdvisory(
                profile = profile,
                weather = weather,
                forecastDays = forecastDays
            )
        }
    }

    private fun buildLocationKey(profile: FarmerProfile): String {
        return "${profile.district}_${profile.state}".lowercase()
    }

    private companion object {
        const val ADVISORY_CACHE_TTL_MS: Long = 60L * 60L * 1000L
    }
}
