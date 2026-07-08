package com.kisanalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanalert.data.local.entity.AlertEntity
import com.kisanalert.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey LIMIT 1")
    suspend fun getWeather(locationKey: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCacheEntity)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun observeAlerts(userId: String, limit: Int = 10): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAlerts(userId: String, limit: Int = 10): List<AlertEntity>

    @Query("SELECT COUNT(*) FROM alerts WHERE userId = :userId")
    suspend fun getAlertCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: String)

    @Query("UPDATE alerts SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAlertsAsRead(userId: String)

    @Query("SELECT COUNT(*) FROM alerts WHERE userId = :userId AND isRead = 0")
    fun observeUnreadCount(userId: String): Flow<Int>
}
