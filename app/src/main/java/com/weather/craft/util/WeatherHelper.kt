package com.weather.craft.util

fun getWeatherCondition(code: Int): String {
    return when (code) {
        0 -> "Clear Sky"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snowfall"
        80, 81, 82 -> "Rain Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Severe Thunderstorm"
        else -> "Overcast"
    }
}
