package com.kisanalert.data.remote

import com.kisanalert.data.remote.api.OpenMeteoGeocodingApi
import com.kisanalert.data.remote.dto.OpenMeteoGeocodingResultDto
import com.kisanalert.domain.model.FarmerProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerGeocodingDataSource @Inject constructor(
    private val openMeteoGeocodingApi: OpenMeteoGeocodingApi
) {
    suspend fun resolveCoordinates(profile: FarmerProfile): Pair<Double, Double>? {
        if (profile.latitude != null && profile.longitude != null) {
            return profile.latitude to profile.longitude
        }
        val villageCoordinates = geocodeLocation(
            query = profile.village,
            state = profile.state
        )
        if (villageCoordinates != null) {
            return villageCoordinates
        }
        return geocodeLocation(
            query = profile.district,
            state = profile.state
        )
    }

    private suspend fun geocodeLocation(
        query: String,
        state: String
    ): Pair<Double, Double>? {
        if (query.isBlank()) {
            return null
        }
        return try {
            val response = openMeteoGeocodingApi.searchLocation(name = query)
            val results = response.results.orEmpty()
            if (results.isEmpty()) {
                return null
            }
            val bestMatch = selectBestMatch(
                results = results,
                state = state
            )
            bestMatch.latitude to bestMatch.longitude
        } catch (exception: Exception) {
            null
        }
    }

    private fun selectBestMatch(
        results: List<OpenMeteoGeocodingResultDto>,
        state: String
    ): OpenMeteoGeocodingResultDto {
        val normalizedState = state.trim().lowercase()
        return results.firstOrNull { result ->
            result.admin1?.trim()?.lowercase() == normalizedState
        } ?: results.first()
    }
}
