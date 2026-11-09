package com.weather.craft.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentUnitsData?,
    val hourly: HourlyData?,
    val daily: DailyData?
)

data class CurrentUnitsData(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("apparent_temperature") val apparentTemperature: Double,
    @SerializedName("precipitation") val precipitation: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("surface_pressure") val pressure: Double
)

data class HourlyData(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperature2m: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>
)

data class DailyData(
    val time: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>,
    val sunrise: List<String>?,
    val sunset: List<String>?,
    @SerializedName("uv_index_max") val uvIndex: List<Double>?
)

data class AirQualityResponse(
    val current: CurrentAqiData?
)

data class CurrentAqiData(
    @SerializedName("european_aqi") val europeanAqi: Int?,
    @SerializedName("us_aqi") val usAqi: Int?,
    val pm10: Double?,
    @SerializedName("pm2_5") val pm25: Double?
)

interface WeatherService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,surface_pressure",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

interface AirQualityService {
    @GET("v1/air-quality")
    suspend fun getAqi(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "european_aqi,us_aqi,pm10,pm2_5"
    ): AirQualityResponse
}

object NetworkClient {
    val weatherApi: WeatherService = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherService::class.java)

    val aqiApi: AirQualityService = Retrofit.Builder()
        .baseUrl("https://air-quality-api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AirQualityService::class.java)
}
