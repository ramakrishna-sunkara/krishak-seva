package com.kisanalert.di

import com.kisanalert.BuildConfig
import com.kisanalert.core.constants.WeatherConstants
import com.kisanalert.data.remote.api.IndiaPostApi
import com.kisanalert.data.remote.api.OpenMeteoApi
import com.kisanalert.data.remote.api.OpenMeteoGeocodingApi
import com.kisanalert.data.remote.api.OpenWeatherApi
import com.kisanalert.data.repository.DashboardRepositoryImpl
import com.kisanalert.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenWeatherRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenMeteoApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): OpenMeteoApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(WeatherConstants.OPEN_METEO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenMeteoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenMeteoGeocodingApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): OpenMeteoGeocodingApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(WeatherConstants.OPEN_METEO_GEOCODING_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenWeatherApi(retrofit: Retrofit): OpenWeatherApi {
        return retrofit.create(OpenWeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIndiaPostApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): IndiaPostApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.INDIA_POST_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(IndiaPostApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository
}
