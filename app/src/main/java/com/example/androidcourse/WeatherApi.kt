package com.example.androidcourse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double = 43.12,
        @Query("longitude") longitude: Double = 131.89,
        @Query("current") current: String = "temperature_2m,wind_speed_10m"
    ): WeatherResponseDto
}

@Serializable
data class WeatherResponseDto(
    val current: CurrentWeatherDto
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperatureC: Double,
    @SerialName("wind_speed_10m") val windSpeedKmh: Double
)

data class StationWeather(
    val temperatureC: Double,
    val windSpeedKmh: Double
) {
    val coolingPower: Int
        get() = when {
            temperatureC <= 0.0 -> 45
            temperatureC >= 25.0 -> 25
            else -> 35
        }
}