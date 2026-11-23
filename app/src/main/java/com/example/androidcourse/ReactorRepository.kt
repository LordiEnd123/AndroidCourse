package com.example.androidcourse

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class ReactorRepository(private val weatherApi: WeatherApi) {

    suspend fun fetchStationWeather(): StationWeather {
        val dto = retryTemporaryErrors { weatherApi.getCurrentWeather() }
        return StationWeather(
            temperatureC = dto.current.temperatureC,
            windSpeedKmh = dto.current.windSpeedKmh
        )
    }

    private suspend fun <T> retryTemporaryErrors(
        times: Int = 3,
        initialDelayMs: Long = 500L,
        block: suspend () -> T
    ): T {
        var pause = initialDelayMs
        repeat(times - 1) {
            try {
                return block()
            } catch (e: IOException) {

            } catch (e: HttpException) {
                if (e.code() < 500) throw e
            }
            delay(pause)
            pause *= 2
        }
        return block()
    }
}