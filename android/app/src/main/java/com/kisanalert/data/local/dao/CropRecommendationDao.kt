package com.kisanalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanalert.data.local.entity.CropRecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropRecommendationDao {
    @Query(
        "SELECT * FROM crop_recommendations WHERE userId = :userId AND season = :season " +
            "ORDER BY generatedAt DESC"
    )
    fun observeRecommendations(userId: String, season: String): Flow<List<CropRecommendationEntity>>

    @Query(
        "SELECT * FROM crop_recommendations WHERE userId = :userId AND season = :season " +
            "ORDER BY generatedAt DESC"
    )
    suspend fun getRecommendations(userId: String, season: String): List<CropRecommendationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<CropRecommendationEntity>)

    @Query("DELETE FROM crop_recommendations WHERE userId = :userId AND season = :season")
    suspend fun deleteRecommendationsForSeason(userId: String, season: String)
}
