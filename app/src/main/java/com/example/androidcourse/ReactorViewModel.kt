package com.example.androidcourse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class ReactorViewModel(
    private val repository: ReactorRepository
) : ViewModel() {
    private var nextId = 11
    private var weatherJob: Job? = null

    var uiState by mutableStateOf(
        ReactorNetworkUiState(
            reactors = (1..10).map { id -> Reactor(id = id, name = "Генератор $id") }
        )
    )
        private set

    init {
        loadWeather()
    }

    fun loadWeather() {
        if (weatherJob?.isActive == true) return
        weatherJob = viewModelScope.launch {
            uiState = uiState.copy(weatherState = WeatherUiState.Loading)
            val result = try {
                WeatherUiState.Content(repository.fetchStationWeather())
            } catch (e: IOException) {
                WeatherUiState.Error(
                    message = "Нет связи с метеостанцией. Охлаждение: штатное (-35°C)",
                    canRetry = true
                )
            } catch (e: HttpException) {
                val canRetry = e.code() >= 500
                WeatherUiState.Error(
                    message = "Ошибка сервера (${e.code()}). Охлаждение: штатное (-35°C)",
                    canRetry = canRetry
                )
            } catch (e: SerializationException) {
                WeatherUiState.Error(
                    message = "Некорректный формат данных метеостанции",
                    canRetry = false
                )
            }
            uiState = uiState.copy(weatherState = result)
        }
    }

    fun addReactor() {
        val id = nextId++
        uiState = uiState.copy(
            reactors = uiState.reactors + Reactor(id = id, name = "Генератор $id")
        )
    }

    fun removeReactor(id: Int) {
        uiState = uiState.copy(
            reactors = uiState.reactors.filterNot { it.id == id }
        )
    }

    fun generatePower(id: Int) = update(id, Reactor::generatePower)

    fun coolDown(id: Int) {
        val power = uiState.currentCoolingPower
        update(id) { it.coolDown(power) }
    }

    fun refuel(id: Int) = update(id, Reactor::refuel)
    fun repair(id: Int) = update(id, Reactor::repair)
    fun reset(id: Int) = update(id, Reactor::reset)

    private fun update(id: Int, transform: (Reactor) -> Reactor) {
        uiState = uiState.copy(
            reactors = uiState.reactors.map { if (it.id == id) transform(it) else it }
        )
    }
}