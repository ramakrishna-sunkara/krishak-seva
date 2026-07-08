package com.kisanalert.data.mapper

import com.kisanalert.data.local.entity.AlertEntity
import com.kisanalert.data.local.entity.WeatherCacheEntity
import com.kisanalert.data.remote.dto.OpenWeatherResponseDto
import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.model.WeatherSummary

fun OpenWeatherResponseDto.toWeatherSummary(): WeatherSummary {
    val rainVolume = rain?.oneHourMm ?: rain?.threeHourMm ?: 0.0
    val windKmh = wind.speedMetersPerSecond * 3.6
    return WeatherSummary(
        temperatureCelsius = main.temp,
        humidityPercent = main.humidity,
        rainVolumeMm = rainVolume,
        windSpeedKmh = windKmh,
        description = weather.firstOrNull()?.description?.replaceFirstChar { char ->
            char.uppercase()
        } ?: "Clear",
        cityName = name,
        iconCode = weather.firstOrNull()?.icon.orEmpty()
    )
}

fun WeatherSummary.toEntity(locationKey: String): WeatherCacheEntity {
    return WeatherCacheEntity(
        locationKey = locationKey,
        temperatureCelsius = temperatureCelsius,
        humidityPercent = humidityPercent,
        rainVolumeMm = rainVolumeMm,
        windSpeedKmh = windSpeedKmh,
        description = description,
        cityName = cityName,
        iconCode = iconCode,
        updatedAt = updatedAt
    )
}

fun WeatherCacheEntity.toDomain(): WeatherSummary {
    return WeatherSummary(
        temperatureCelsius = temperatureCelsius,
        humidityPercent = humidityPercent,
        rainVolumeMm = rainVolumeMm,
        windSpeedKmh = windSpeedKmh,
        description = description,
        cityName = cityName,
        iconCode = iconCode,
        updatedAt = updatedAt
    )
}

fun AlertEntity.toDomain(): DashboardAlert {
    return DashboardAlert(
        id = id,
        title = title,
        message = message,
        type = AlertType.valueOf(type),
        timestamp = timestamp,
        isRead = isRead
    )
}

fun DashboardAlert.toEntity(userId: String): AlertEntity {
    return AlertEntity(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type.name,
        timestamp = timestamp,
        isRead = isRead
    )
}
