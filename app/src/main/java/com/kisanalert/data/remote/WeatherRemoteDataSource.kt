package com.kisanalert.data.remote

import com.kisanalert.BuildConfig
import com.kisanalert.core.constants.WeatherConstants
import com.kisanalert.data.local.dao.WeatherCacheDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.mapper.toForecastDays
import com.kisanalert.data.mapper.toWeatherSummary
import com.kisanalert.data.remote.api.OpenMeteoApi
import com.kisanalert.data.remote.api.OpenWeatherApi
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.WeatherForecastDay
import com.kisanalert.domain.model.WeatherSummary
import com.kisanalert.domain.repository.FarmerProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRemoteDataSource @Inject constructor(
    private val openMeteoApi: OpenMeteoApi,
    private val openWeatherApi: OpenWeatherApi,
    private val weatherCacheDao: WeatherCacheDao,
    private val farmerGeocodingDataSource: FarmerGeocodingDataSource,
    private val farmerProfileRepository: FarmerProfileRepository
) {
    suspend fun loadWeatherData(
        profile: FarmerProfile,
        locationKey: String,
        forceRefresh: Boolean
    ): WeatherDataResult {
        if (!forceRefresh) {
            val cachedWeather = weatherCacheDao.getWeather(locationKey)?.toDomain()
            if (cachedWeather != null && isCacheValid(cachedWeather.updatedAt)) {
                val forecastDays = loadForecastDays(profile = profile, forceLiveFetch = true)
                return WeatherDataResult(
                    summary = cachedWeather,
                    forecastDays = forecastDays,
                    isFromCache = true
                )
            }
        }
        val resolvedProfile = ensureProfileCoordinates(profile)
        if (WeatherConstants.USE_OPEN_METEO_PRIMARY &&
            resolvedProfile.latitude != null &&
            resolvedProfile.longitude != null
        ) {
            val openMeteoResult = fetchFromOpenMeteo(
                profile = resolvedProfile,
                locationKey = locationKey
            )
            if (openMeteoResult != null) {
                return openMeteoResult
            }
        }
        return fetchFromOpenWeather(
            profile = resolvedProfile,
            locationKey = locationKey
        )
    }

    suspend fun loadForecastDays(
        profile: FarmerProfile,
        forceLiveFetch: Boolean = false
    ): List<WeatherForecastDay> {
        val resolvedProfile = ensureProfileCoordinates(profile)
        if (WeatherConstants.USE_OPEN_METEO_PRIMARY &&
            resolvedProfile.latitude != null &&
            resolvedProfile.longitude != null
        ) {
            try {
                val response = openMeteoApi.getForecast(
                    latitude = resolvedProfile.latitude!!,
                    longitude = resolvedProfile.longitude!!,
                    forecastDays = WeatherConstants.OPEN_METEO_FORECAST_DAYS
                )
                val summary = response.toWeatherSummary(cityName = resolvedProfile.district)
                return response.toForecastDays(defaultHumidityPercent = summary.humidityPercent)
            } catch (exception: Exception) {
                // Fall through to OpenWeather.
            }
        }
        return fetchOpenWeatherForecast(resolvedProfile)
    }

    private suspend fun fetchFromOpenMeteo(
        profile: FarmerProfile,
        locationKey: String
    ): WeatherDataResult? {
        return try {
            val latitude = profile.latitude ?: return null
            val longitude = profile.longitude ?: return null
            val response = openMeteoApi.getForecast(
                latitude = latitude,
                longitude = longitude,
                forecastDays = WeatherConstants.OPEN_METEO_FORECAST_DAYS
            )
            val summary = response.toWeatherSummary(cityName = profile.district)
            val forecastDays = response.toForecastDays(defaultHumidityPercent = summary.humidityPercent)
            weatherCacheDao.insertWeather(summary.toEntity(locationKey))
            WeatherDataResult(
                summary = summary,
                forecastDays = forecastDays,
                isFromCache = false
            )
        } catch (exception: Exception) {
            null
        }
    }

    private suspend fun fetchFromOpenWeather(
        profile: FarmerProfile,
        locationKey: String
    ): WeatherDataResult {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
        if (apiKey.isBlank()) {
            val fallbackWeather = weatherCacheDao.getWeather(locationKey)?.toDomain()
                ?: buildFallbackWeather(profile)
            return WeatherDataResult(
                summary = fallbackWeather,
                forecastDays = emptyList(),
                isFromCache = true
            )
        }
        return try {
            val query = "${profile.district},${profile.state},IN"
            val currentResponse = openWeatherApi.getCurrentWeather(query = query, apiKey = apiKey)
            val summary = currentResponse.toWeatherSummary()
            weatherCacheDao.insertWeather(summary.toEntity(locationKey))
            val forecastDays = fetchOpenWeatherForecast(profile)
            WeatherDataResult(
                summary = summary,
                forecastDays = forecastDays,
                isFromCache = false
            )
        } catch (exception: Exception) {
            val cachedWeather = weatherCacheDao.getWeather(locationKey)?.toDomain()
                ?: buildFallbackWeather(profile)
            WeatherDataResult(
                summary = cachedWeather,
                forecastDays = emptyList(),
                isFromCache = true
            )
        }
    }

    private suspend fun fetchOpenWeatherForecast(profile: FarmerProfile): List<WeatherForecastDay> {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
        if (apiKey.isBlank()) {
            return emptyList()
        }
        return try {
            val query = "${profile.district},${profile.state},IN"
            val response = openWeatherApi.getWeatherForecast(query = query, apiKey = apiKey)
            response.toForecastDays()
        } catch (exception: Exception) {
            emptyList()
        }
    }

    private suspend fun ensureProfileCoordinates(profile: FarmerProfile): FarmerProfile {
        if (profile.latitude != null && profile.longitude != null) {
            return profile
        }
        val coordinates = farmerGeocodingDataSource.resolveCoordinates(profile) ?: return profile
        val updatedProfile = profile.copy(
            latitude = coordinates.first,
            longitude = coordinates.second,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        farmerProfileRepository.saveProfile(updatedProfile)
        return updatedProfile
    }

    private fun buildFallbackWeather(profile: FarmerProfile): WeatherSummary {
        return WeatherSummary(
            temperatureCelsius = 32.0,
            humidityPercent = 65,
            rainVolumeMm = 0.0,
            windSpeedKmh = 12.0,
            description = "Partly cloudy",
            cityName = profile.district,
            iconCode = "02d"
        )
    }

    private fun isCacheValid(updatedAt: Long): Boolean {
        val cacheAgeMs = System.currentTimeMillis() - updatedAt
        return cacheAgeMs < WeatherConstants.WEATHER_CACHE_TTL_MS
    }
}

data class WeatherDataResult(
    val summary: WeatherSummary,
    val forecastDays: List<WeatherForecastDay>,
    val isFromCache: Boolean
)
