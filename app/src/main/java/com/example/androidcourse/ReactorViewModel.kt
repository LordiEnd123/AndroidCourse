package com.example.androidcourse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ReactorViewModel : ViewModel() {
    private var nextId = 11

    var uiState by mutableStateOf(
        ReactorNetworkUiState(
            reactors = (1..10).map { id -> Reactor(id = id, name = "Генератор $id") }
        )
    )
        private set

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
    fun coolDown(id: Int) = update(id, Reactor::coolDown)
    fun refuel(id: Int) = update(id, Reactor::refuel)
    fun repair(id: Int) = update(id, Reactor::repair)
    fun reset(id: Int) = update(id, Reactor::reset)

    private fun update(id: Int, transform: (Reactor) -> Reactor) {
        uiState = uiState.copy(
            reactors = uiState.reactors.map { if (it.id == id) transform(it) else it }
        )
    }
}