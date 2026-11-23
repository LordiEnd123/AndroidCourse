package com.example.androidcourse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReactorApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object ReactorListRoute : AppRoute

@Serializable
data class ReactorDetailRoute(val reactorId: Int) : AppRoute

@Composable
fun ReactorApp(
    modifier: Modifier = Modifier,
    vm: ReactorViewModel = viewModel())
{
    val backStack = rememberNavBackStack(ReactorListRoute)

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = ::goBack,
        modifier = modifier,
        entryProvider = { key ->
            when (key) {
                is ReactorListRoute -> NavEntry(key) {
                    ReactorListScreen(
                        state = vm.uiState,
                        onItemClick = { id ->
                            val target = ReactorDetailRoute(id)
                            if (backStack.lastOrNull() != target) {
                                backStack.add(target)
                            }
                        },
                        onAdd = vm::addReactor,
                        onDelete = vm::removeReactor)
                }

                is ReactorDetailRoute -> NavEntry(key) {
                    val reactor = vm.uiState.reactors.find { it.id == key.reactorId }
                    if (reactor != null) {
                        ReactorDetailScreen(
                            state = reactor,
                            onGenerate = { vm.generatePower(reactor.id) },
                            onCoolDown = { vm.coolDown(reactor.id) },
                            onRefuel = { vm.refuel(reactor.id) },
                            onRepair = { vm.repair(reactor.id) },
                            onReset = { vm.reset(reactor.id) },
                            onBack = ::goBack)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center)
                        {
                            Text("Генератор не найден")
                            Button(onClick = ::goBack) { Text("Назад") }
                        }
                    }
                }

                else -> NavEntry(key) { Text("Экран не найден") }
            }
        }
    )
}