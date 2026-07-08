package com.kisanalert.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kisanalert.data.local.dao.AlertDao
import com.kisanalert.data.local.dao.CropDoctorScanDao
import com.kisanalert.data.local.dao.CropRecommendationDao
import com.kisanalert.data.local.dao.FarmerProfileDao
import com.kisanalert.data.local.dao.VoiceMessageDao
import com.kisanalert.data.local.dao.WeatherAdvisoryCacheDao
import com.kisanalert.data.local.dao.WeatherCacheDao
import com.kisanalert.data.local.entity.AlertEntity
import com.kisanalert.data.local.entity.CropDoctorScanEntity
import com.kisanalert.data.local.entity.CropRecommendationEntity
import com.kisanalert.data.local.entity.FarmerProfileEntity
import com.kisanalert.data.local.entity.VoiceMessageEntity
import com.kisanalert.data.local.entity.WeatherAdvisoryCacheEntity
import com.kisanalert.data.local.entity.WeatherCacheEntity

@Database(
    entities = [
        FarmerProfileEntity::class,
        WeatherCacheEntity::class,
        AlertEntity::class,
        CropRecommendationEntity::class,
        WeatherAdvisoryCacheEntity::class,
        CropDoctorScanEntity::class,
        VoiceMessageEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class KisanDatabase : RoomDatabase() {
    abstract fun farmerProfileDao(): FarmerProfileDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun alertDao(): AlertDao
    abstract fun cropRecommendationDao(): CropRecommendationDao
    abstract fun weatherAdvisoryCacheDao(): WeatherAdvisoryCacheDao
    abstract fun cropDoctorScanDao(): CropDoctorScanDao
    abstract fun voiceMessageDao(): VoiceMessageDao
}
