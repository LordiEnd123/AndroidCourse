package com.example.androidcourse

data class Reactor(
    val id: Int,
    val name: String,
    val fuel: Int = 80,
    val maxFuel: Int = 100,
    val temperature: Int = 30,
    val maxTemp: Int = 120,
    val hull: Int = 100,
    val maxHull: Int = 100,
    val totalEnergy: Int = 0)
{
    val isBroken: Boolean get() = hull <= 0
    val isOverheated: Boolean get() = !isBroken && temperature >= 85
    val isLowFuel: Boolean get() = !isBroken && fuel < 20
    val isSafeForMaintenance: Boolean get() = !isBroken && temperature <= 50

    val canGenerate: Boolean get() = !isBroken && fuel >= 20 && temperature <= 105
    val canCoolDown: Boolean get() = !isBroken && temperature > 20 && fuel >= 10
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
            totalEnergy = totalEnergy + 50)
    }

    fun coolDown(): Reactor {
        if (!canCoolDown) return this
        return copy(
            fuel = (fuel - 10).coerceAtLeast(0),
            temperature = (temperature - 35).coerceAtLeast(20))
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
    val reactors: List<Reactor> = emptyList())
{
    val totalEnergy: Int get() = reactors.sumOf { it.totalEnergy }
    val activeCount: Int get() = reactors.count { it.canGenerate }
}