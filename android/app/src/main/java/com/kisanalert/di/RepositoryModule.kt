package com.kisanalert.di

import com.kisanalert.data.repository.AppSettingsRepositoryImpl
import com.kisanalert.data.repository.AuthRepositoryImpl
import com.kisanalert.data.repository.PincodeRepositoryImpl
import com.kisanalert.data.repository.SessionRepositoryImpl
import com.kisanalert.domain.repository.AppSettingsRepository
import com.kisanalert.domain.repository.AuthRepository
import com.kisanalert.domain.repository.PincodeRepository
import com.kisanalert.data.repository.GroundWaterAssessmentRepositoryImpl
import com.kisanalert.domain.repository.GroundWaterAssessmentRepository
import com.kisanalert.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        appSettingsRepositoryImpl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindPincodeRepository(
        pincodeRepositoryImpl: PincodeRepositoryImpl
    ): PincodeRepository

    @Binds
    @Singleton
    abstract fun bindGroundWaterAssessmentRepository(
        groundWaterAssessmentRepositoryImpl: GroundWaterAssessmentRepositoryImpl
    ): GroundWaterAssessmentRepository
}
