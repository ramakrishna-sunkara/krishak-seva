package com.kisanalert.data.mapper

import com.kisanalert.core.utils.WeatherCodeMapper
import com.kisanalert.data.remote.dto.OpenMeteoForecastResponseDto
import com.kisanalert.domain.model.WeatherForecastDay
import com.kisanalert.domain.model.WeatherSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun OpenMeteoForecastResponseDto.toWeatherSummary(cityName: String): WeatherSummary {
    val condition = WeatherCodeMapper.mapWmoCode(current.weather_code)
    return WeatherSummary(
        temperatureCelsius = current.temperature_2m,
        humidityPercent = current.relative_humidity_2m,
        rainVolumeMm = current.precipitation,
        windSpeedKmh = current.wind_speed_10m,
        description = condition.description,
        cityName = cityName,
        iconCode = condition.iconCode
    )
}

fun OpenMeteoForecastResponseDto.toForecastDays(defaultHumidityPercent: Int): List<WeatherForecastDay> {
    val dayCount: Int = minOf(
        daily.time.size,
        daily.temperature_2m_max.size,
        daily.temperature_2m_min.size,
        daily.precipitation_sum.size,
        daily.weather_code.size
    )
    return (0 until dayCount).map { index ->
        val forecastDate = LocalDate.parse(daily.time[index], DateTimeFormatter.ISO_LOCAL_DATE)
        val condition = WeatherCodeMapper.mapWmoCode(daily.weather_code[index])
        val averageTemperature: Double =
            (daily.temperature_2m_max[index] + daily.temperature_2m_min[index]) / 2.0
        WeatherForecastDay(
            dayLabel = buildForecastDayLabel(forecastDate),
            temperatureCelsius = averageTemperature,
            description = condition.description,
            humidityPercent = defaultHumidityPercent,
            rainVolumeMm = daily.precipitation_sum[index],
            iconCode = condition.iconCode
        )
    }
}

private fun buildForecastDayLabel(forecastDate: LocalDate): String {
    val today = LocalDate.now()
    val daysFromToday = ChronoUnit.DAYS.between(today, forecastDate)
    return when (daysFromToday) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> forecastDate.dayOfWeek.name.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
}
