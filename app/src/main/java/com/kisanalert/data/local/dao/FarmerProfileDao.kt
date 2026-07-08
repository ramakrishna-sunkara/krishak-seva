package com.kisanalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanalert.data.local.entity.FarmerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerProfileDao {
    @Query("SELECT * FROM farmer_profiles WHERE userId = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<FarmerProfileEntity?>

    @Query("SELECT * FROM farmer_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfile(userId: String): FarmerProfileEntity?

    @Query("SELECT * FROM farmer_profiles WHERE isSynced = 0")
    suspend fun getUnsyncedProfiles(): List<FarmerProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FarmerProfileEntity)

    @Query("DELETE FROM farmer_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}
