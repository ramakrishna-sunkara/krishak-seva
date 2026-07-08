package com.kisanalert.data.repository

import com.kisanalert.core.constants.AppConstants
import com.kisanalert.core.utils.Result
import com.kisanalert.data.local.dao.FarmerProfileDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toEntity
import com.kisanalert.data.remote.FarmerGeocodingDataSource
import com.kisanalert.data.remote.firebase.FirestoreFarmerProfileDataSource
import com.kisanalert.di.ApplicationScope
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.repository.FarmerProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerProfileRepositoryImpl @Inject constructor(
    private val farmerProfileDao: FarmerProfileDao,
    private val firestoreFarmerProfileDataSource: FirestoreFarmerProfileDataSource,
    private val farmerGeocodingDataSource: FarmerGeocodingDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : FarmerProfileRepository {
    override fun observeProfile(userId: String): Flow<FarmerProfile?> {
        return farmerProfileDao.observeProfile(userId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getProfile(userId: String): FarmerProfile? {
        val localProfile = farmerProfileDao.getProfile(userId)?.toDomain()
        if (localProfile != null) {
            return localProfile
        }
        return try {
            withTimeout(AppConstants.FIRESTORE_SYNC_TIMEOUT_MS) {
                val remoteProfile = firestoreFarmerProfileDataSource.getProfile(userId)
                if (remoteProfile != null) {
                    farmerProfileDao.insertProfile(remoteProfile.toEntity())
                }
                remoteProfile
            }
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun saveProfile(profile: FarmerProfile): Result<FarmerProfile> {
        val profileWithCoordinates = resolveCoordinatesIfNeeded(profile)
        val localProfile = profileWithCoordinates.copy(
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        farmerProfileDao.insertProfile(localProfile.toEntity())
        applicationScope.launch {
            syncProfileToFirestore(localProfile)
        }
        return Result.Success(localProfile)
    }

    override suspend fun syncPendingProfiles(): Result<Unit> {
        return try {
            val unsyncedProfiles = farmerProfileDao.getUnsyncedProfiles()
            unsyncedProfiles.forEach { entity ->
                syncProfileToFirestore(entity.toDomain())
            }
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(
                message = exception.localizedMessage ?: "Profile sync failed.",
                exception = exception
            )
        }
    }

    private suspend fun resolveCoordinatesIfNeeded(profile: FarmerProfile): FarmerProfile {
        if (profile.latitude != null && profile.longitude != null) {
            return profile
        }
        val coordinates = farmerGeocodingDataSource.resolveCoordinates(profile) ?: return profile
        return profile.copy(
            latitude = coordinates.first,
            longitude = coordinates.second
        )
    }

    private suspend fun syncProfileToFirestore(profile: FarmerProfile) {
        try {
            withTimeout(AppConstants.FIRESTORE_SYNC_TIMEOUT_MS) {
                firestoreFarmerProfileDataSource.saveProfile(profile)
            }
            val syncedProfile = profile.copy(isSynced = true)
            farmerProfileDao.insertProfile(syncedProfile.toEntity())
        } catch (exception: Exception) {
            // Keep local copy; will retry on next syncPendingProfiles call.
        }
    }
}
