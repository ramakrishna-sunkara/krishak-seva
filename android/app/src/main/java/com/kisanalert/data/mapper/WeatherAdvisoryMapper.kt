package com.kisanalert.data.mapper

import com.kisanalert.data.local.entity.WeatherAdvisoryCacheEntity
import com.kisanalert.data.remote.dto.OpenWeatherForecastItemDto
import com.kisanalert.data.remote.dto.OpenWeatherForecastResponseDto
import com.kisanalert.domain.model.WeatherAdvisoryInsight
import com.kisanalert.domain.model.WeatherForecastDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val ALERT_TIPS_DELIMITER: String = "|||"

fun OpenWeatherForecastResponseDto.toForecastDays(): List<WeatherForecastDay> {
    val noonItems = list
        .filter { item -> item.dateTimeText.contains("12:00:00") }
        .distinctBy { item -> item.dateTimeText.substringBefore(" ") }
        .take(5)
    if (noonItems.isNotEmpty()) {
        return noonItems.map { item -> item.toForecastDay() }
    }
    return list
        .distinctBy { item -> item.dateTimeText.substringBefore(" ") }
        .take(5)
        .map { item -> item.toForecastDay() }
}

private fun OpenWeatherForecastItemDto.toForecastDay(): WeatherForecastDay {
    val rainVolume = rain?.threeHourMm ?: rain?.oneHourMm ?: 0.0
    val windKmh = wind.speedMetersPerSecond * 3.6
    val forecastDate = LocalDateTime.parse(
        dateTimeText,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    ).toLocalDate()
    return WeatherForecastDay(
        dayLabel = buildDayLabel(forecastDate),
        temperatureCelsius = main.temp,
        description = weather.firstOrNull()?.description?.replaceFirstChar { char ->
            char.uppercase()
        } ?: "Clear",
        humidityPercent = main.humidity,
        rainVolumeMm = rainVolume,
        iconCode = weather.firstOrNull()?.icon.orEmpty()
    )
}

private fun buildDayLabel(forecastDate: LocalDate): String {
    val today = LocalDate.now()
    val daysFromToday = ChronoUnit.DAYS.between(today, forecastDate)
    return when (daysFromToday) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> forecastDate.dayOfWeek.name.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
}

fun WeatherAdvisoryInsight.toEntity(locationKey: String): WeatherAdvisoryCacheEntity {
    return WeatherAdvisoryCacheEntity(
        locationKey = locationKey,
        summary = summary,
        irrigationAdvice = irrigationAdvice,
        cropRiskLevel = cropRiskLevel,
        tomorrowOutlook = tomorrowOutlook,
        alertTipsJson = alertTips.joinToString(ALERT_TIPS_DELIMITER),
        isFromCloud = isFromCloud,
        updatedAt = System.currentTimeMillis()
    )
}

fun WeatherAdvisoryCacheEntity.toDomain(): WeatherAdvisoryInsight {
    val alertTips = if (alertTipsJson.isBlank()) {
        emptyList()
    } else {
        alertTipsJson.split(ALERT_TIPS_DELIMITER).filter { tip -> tip.isNotBlank() }
    }
    return WeatherAdvisoryInsight(
        summary = summary,
        irrigationAdvice = irrigationAdvice,
        cropRiskLevel = cropRiskLevel,
        tomorrowOutlook = tomorrowOutlook,
        alertTips = alertTips,
        isFromCloud = isFromCloud
    )
}
