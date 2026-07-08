package com.kisanalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanalert.data.local.entity.WeatherAdvisoryCacheEntity

@Dao
interface WeatherAdvisoryCacheDao {
    @Query("SELECT * FROM weather_advisory_cache WHERE locationKey = :locationKey LIMIT 1")
    suspend fun getAdvisory(locationKey: String): WeatherAdvisoryCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisory(advisory: WeatherAdvisoryCacheEntity)
}
