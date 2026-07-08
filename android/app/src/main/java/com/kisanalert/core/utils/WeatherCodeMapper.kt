package com.kisanalert.core.utils

data class WeatherCondition(
    val description: String,
    val iconCode: String
)

object WeatherCodeMapper {
    fun mapWmoCode(weatherCode: Int): WeatherCondition {
        return when (weatherCode) {
            0 -> WeatherCondition(description = "Clear sky", iconCode = "01d")
            1 -> WeatherCondition(description = "Mainly clear", iconCode = "02d")
            2 -> WeatherCondition(description = "Partly cloudy", iconCode = "03d")
            3 -> WeatherCondition(description = "Overcast", iconCode = "04d")
            45, 48 -> WeatherCondition(description = "Fog", iconCode = "50d")
            51, 53, 55 -> WeatherCondition(description = "Drizzle", iconCode = "09d")
            56, 57 -> WeatherCondition(description = "Freezing drizzle", iconCode = "09d")
            61, 63, 65 -> WeatherCondition(description = "Rain", iconCode = "10d")
            66, 67 -> WeatherCondition(description = "Freezing rain", iconCode = "13d")
            71, 73, 75, 77 -> WeatherCondition(description = "Snow", iconCode = "13d")
            80, 81, 82 -> WeatherCondition(description = "Rain showers", iconCode = "09d")
            85, 86 -> WeatherCondition(description = "Snow showers", iconCode = "13d")
            95 -> WeatherCondition(description = "Thunderstorm", iconCode = "11d")
            96, 99 -> WeatherCondition(description = "Thunderstorm with hail", iconCode = "11d")
            else -> WeatherCondition(description = "Partly cloudy", iconCode = "02d")
        }
    }
}
