package com.example.androidcourse

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Content(val weather: StationWeather) : WeatherUiState
    data class Error(val message: String, val canRetry: Boolean) : WeatherUiState
}

data class Reactor(
    val id: Int,
    val name: String,
    val fuel: Int = 80,
    val maxFuel: Int = 100,
    val temperature: Int = 30,
    val maxTemp: Int = 120,
    val hull: Int = 100,
    val maxHull: Int = 100,
    val totalEnergy: Int = 0
) {
    val isBroken: Boolean get() = hull <= 0
    val isOverheated: Boolean get() = !isBroken && temperature >= 85
    val isLowFuel: Boolean get() = !isBroken && fuel < 20
    val isSafeForMaintenance: Boolean get() = !isBroken && temperature <= 50

    val canGenerate: Boolean get() = !isBroken && fuel >= 20 && temperature <= 105
    val canCoolDown: Boolean get() = !isBroken && temperature > 20
    val canRefuel: Boolean get() = isSafeForMaintenance && fuel < maxFuel
    val canRepair: Boolean get() = isSafeForMaintenance && hull < maxHull

    fun generatePower(): Reactor {
        if (!canGenerate) return this
        val newTemperature = (temperature + 25).coerceAtMost(maxTemp)
        val damage = if (newTemperature >= 85) 35 else 0
        return copy(
            fuel = (fuel - 20).coerceAtLeast(0),
            temperature = newTemperature,
            hull = (hull - damage).coerceAtLeast(0),
            totalEnergy = totalEnergy + 50
        )
    }

    fun coolDown(coolingPower: Int = 35): Reactor {
        if (!canCoolDown) return this
        return copy(temperature = (temperature - coolingPower).coerceAtLeast(20))
    }

    fun refuel(): Reactor {
        if (!canRefuel) return this
        return copy(fuel = (fuel + 40).coerceAtMost(maxFuel))
    }

    fun repair(): Reactor {
        if (!canRepair) return this
        return copy(hull = (hull + 30).coerceAtMost(maxHull))
    }

    fun reset(): Reactor = Reactor(id = id, name = name)
}

data class ReactorNetworkUiState(
    val reactors: List<Reactor> = emptyList(),
    val totalEnergy: Int = 0,
    val weatherState: WeatherUiState = WeatherUiState.Loading
) {
    val activeCount: Int get() = reactors.count { it.canGenerate }
    val currentCoolingPower: Int
        get() = (weatherState as? WeatherUiState.Content)?.weather?.coolingPower ?: 35
}