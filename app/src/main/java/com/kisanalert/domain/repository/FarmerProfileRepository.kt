package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.FarmerProfile
import kotlinx.coroutines.flow.Flow

interface FarmerProfileRepository {
    fun observeProfile(userId: String): Flow<FarmerProfile?>
    suspend fun getProfile(userId: String): FarmerProfile?
    suspend fun saveProfile(profile: FarmerProfile): Result<FarmerProfile>
    suspend fun syncPendingProfiles(): Result<Unit>
}
