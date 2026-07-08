package com.kisanalert.di

import android.content.Context
import androidx.room.Room
import com.kisanalert.core.constants.AppConstants
import com.kisanalert.data.local.dao.AlertDao
import com.kisanalert.data.local.dao.CropDoctorScanDao
import com.kisanalert.data.local.dao.CropRecommendationDao
import com.kisanalert.data.local.dao.FarmerProfileDao
import com.kisanalert.data.local.dao.VoiceMessageDao
import com.kisanalert.data.local.dao.WeatherCacheDao
import com.kisanalert.data.local.dao.WeatherAdvisoryCacheDao
import com.kisanalert.data.local.database.KisanDatabase
import com.kisanalert.data.repository.CropDoctorRepositoryImpl
import com.kisanalert.data.repository.CropRecommendationRepositoryImpl
import com.kisanalert.data.repository.FarmerProfileRepositoryImpl
import com.kisanalert.data.repository.WeatherAdvisoryRepositoryImpl
import com.kisanalert.domain.repository.CropDoctorRepository
import com.kisanalert.domain.repository.CropRecommendationRepository
import com.kisanalert.domain.repository.FarmerProfileRepository
import com.kisanalert.domain.repository.WeatherAdvisoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideKisanDatabase(
        @ApplicationContext context: Context
    ): KisanDatabase {
        return Room.databaseBuilder(
            context,
            KisanDatabase::class.java,
            AppConstants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFarmerProfileDao(database: KisanDatabase): FarmerProfileDao {
        return database.farmerProfileDao()
    }

    @Provides
    fun provideWeatherCacheDao(database: KisanDatabase): WeatherCacheDao {
        return database.weatherCacheDao()
    }

    @Provides
    fun provideAlertDao(database: KisanDatabase): AlertDao {
        return database.alertDao()
    }

    @Provides
    fun provideCropRecommendationDao(database: KisanDatabase): CropRecommendationDao {
        return database.cropRecommendationDao()
    }

    @Provides
    fun provideWeatherAdvisoryCacheDao(database: KisanDatabase): WeatherAdvisoryCacheDao {
        return database.weatherAdvisoryCacheDao()
    }

    @Provides
    fun provideCropDoctorScanDao(database: KisanDatabase): CropDoctorScanDao {
        return database.cropDoctorScanDao()
    }

    @Provides
    fun provideVoiceMessageDao(database: KisanDatabase): VoiceMessageDao {
        return database.voiceMessageDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FarmerProfileRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFarmerProfileRepository(
        farmerProfileRepositoryImpl: FarmerProfileRepositoryImpl
    ): FarmerProfileRepository

    @Binds
    @Singleton
    abstract fun bindCropRecommendationRepository(
        cropRecommendationRepositoryImpl: CropRecommendationRepositoryImpl
    ): CropRecommendationRepository

    @Binds
    @Singleton
    abstract fun bindWeatherAdvisoryRepository(
        weatherAdvisoryRepositoryImpl: WeatherAdvisoryRepositoryImpl
    ): WeatherAdvisoryRepository

    @Binds
    @Singleton
    abstract fun bindCropDoctorRepository(
        cropDoctorRepositoryImpl: CropDoctorRepositoryImpl
    ): CropDoctorRepository
}
