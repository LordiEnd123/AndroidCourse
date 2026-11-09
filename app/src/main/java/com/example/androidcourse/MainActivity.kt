package com.example.androidcourse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

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
data object ReactorListRoute
data class ReactorDetailRoute(val reactorId: Int)

// Состояние
data class ReactorUiState(
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
}

// Состояние всей сети генераторов
data class ReactorNetworkUiState(
    val reactors: List<ReactorUiState> = emptyList(),
    val nextId: Int = 11)
{
    val totalEnergy: Int get() = reactors.sumOf { it.totalEnergy }
    val activeCount: Int get() = reactors.count { !it.isBroken }
}

// ViewModel
class ReactorViewModel : ViewModel() {
    var uiState by mutableStateOf(
        ReactorNetworkUiState(
            reactors = (1..10).map { id -> ReactorUiState(id = id, name = "Генератор $id") }
        )
    )
        private set
    val backStack = mutableStateListOf<Any>(ReactorListRoute)
    fun openDetails(id: Int) {
        if (backStack.last() != ReactorDetailRoute(id)) {
            backStack.add(ReactorDetailRoute(id))
        }
    }
    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    fun addReactor() {
        val id = uiState.nextId
        uiState = uiState.copy(
            reactors = uiState.reactors + ReactorUiState(id = id, name = "Генератор $id"),
            nextId = id + 1
        )
    }
    fun removeReactor(id: Int) {
        uiState = uiState.copy(reactors = uiState.reactors.filterNot { it.id == id })
    }
    fun generatePower(id: Int) = update(id) { r ->
        if (!r.canGenerate) return@update r
        val newTemperature = (r.temperature + 25).coerceAtMost(r.maxTemp)
        val damage = if (newTemperature >= 85) 35 else 0
        r.copy(
            fuel = (r.fuel - 20).coerceAtLeast(0),
            temperature = newTemperature,
            hull = (r.hull - damage).coerceAtLeast(0),
            totalEnergy = r.totalEnergy + 50
        )
    }
    fun coolDown(id: Int) = update(id) { r ->
        if (!r.canCoolDown) return@update r
        r.copy(
            fuel = (r.fuel - 10).coerceAtLeast(0),
            temperature = (r.temperature - 35).coerceAtLeast(20)
        )
    }
    fun refuel(id: Int) = update(id) { r ->
        if (!r.canRefuel) return@update r
        r.copy(fuel = (r.fuel + 40).coerceAtMost(r.maxFuel))
    }
    fun repair(id: Int) = update(id) { r ->
        if (!r.canRepair) return@update r
        r.copy(hull = (r.hull + 30).coerceAtMost(r.maxHull))
    }
    fun reset(id: Int) = update(id) { r -> ReactorUiState(id = r.id, name = r.name) }
    private fun update(id: Int, transform: (ReactorUiState) -> ReactorUiState) {
        uiState = uiState.copy(
            reactors = uiState.reactors.map { if (it.id == id) transform(it) else it }
        )
    }
}

// Навигация
@Composable
fun ReactorApp(modifier: Modifier = Modifier, vm: ReactorViewModel = viewModel()) {
    NavDisplay(
        backStack = vm.backStack,
        onBack = vm::goBack,
        modifier = modifier,
        entryProvider = { key ->
            when (key) {
                is ReactorListRoute -> NavEntry(key) {
                    ReactorListScreen(
                        state = vm.uiState,
                        onItemClick = vm::openDetails,
                        onAdd = vm::addReactor,
                        onDelete = vm::removeReactor
                    )
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
                            onBack = vm::goBack
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Генератор не найден")
                            Button(onClick = vm::goBack) { Text("Назад") }
                        }
                    }
                }

                else -> NavEntry(key) { Text("Экран не найден") }
            }
        }
    )
}

// Список генераторов
@Composable
fun ReactorListScreen(
    state: ReactorNetworkUiState,
    onItemClick: (Int) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Управление генераторами", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Активных: ${state.activeCount} | Всего энергии: ${state.totalEnergy} МВт·ч",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Text("+ Добавить новый генератор")
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.reactors, key = { it.id }) { reactor ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg(reactor)),
                    modifier = Modifier.fillMaxWidth().clickable { onItemClick(reactor.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.generator_img),
                            contentDescription = reactor.name,
                            modifier = Modifier
                                .size(50.dp)
                                .alpha(if (reactor.isBroken) 0.4f else 1.0f),
                            colorFilter = if (reactor.isBroken) {
                                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            } else {
                                null
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = reactor.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = statusText(reactor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor(reactor)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Корпус: ${reactor.hull}% | Топливо: ${reactor.fuel} | Ядро: ${reactor.temperature}°C",
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(onClick = { onDelete(reactor.id) }) {
                            Text("Удалить", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// Пульт управления генератором
@Composable
fun ReactorDetailScreen(
    state: ReactorUiState,
    onGenerate: () -> Unit,
    onCoolDown: () -> Unit,
    onRefuel: () -> Unit,
    onRepair: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier)
{
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("← К списку")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg(state)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.generator_img),
                    contentDescription = state.name,
                    modifier = Modifier
                        .size(130.dp)
                        .alpha(if (state.isBroken) 0.4f else 1.0f),
                    colorFilter = if (state.isBroken) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    } else {
                        null
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = statusText(state),
                    fontWeight = FontWeight.Medium,
                    color = statusColor(state),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Выработано: ${state.totalEnergy} МВт·ч", fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Корпус: ${state.hull} / ${state.maxHull}%")
        LinearProgressIndicator(
            progress = { state.hull.toFloat() / state.maxHull },
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Топливо: ${state.fuel} / ${state.maxFuel} ед.")
        LinearProgressIndicator(
            progress = { state.fuel.toFloat() / state.maxFuel },
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Температура ядра: ${state.temperature}°C (макс. ${state.maxTemp}°C)")
        LinearProgressIndicator(
            progress = { state.temperature.toFloat() / state.maxTemp },
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onGenerate, enabled = state.canGenerate, modifier = Modifier.weight(1f)) {
                Text("Форсаж")
            }
            Button(onClick = onCoolDown, enabled = state.canCoolDown, modifier = Modifier.weight(1f)) {
                Text("Охладить")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onRefuel, enabled = state.canRefuel, modifier = Modifier.weight(1f)) {
                Text("Заправить")
            }
            Button(onClick = onRepair, enabled = state.canRepair, modifier = Modifier.weight(1f)) {
                Text("Ремонт")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.isBroken) "Перезапустить генератор" else "Сбросить настройки")
        }
    }
}

fun statusText(r: ReactorUiState) = when {
    r.isBroken -> "Авария: Разрушен"
    r.isOverheated -> "Перегрев"
    r.isLowFuel -> "Мало топлива"
    else -> "Штатный режим"
}
fun statusColor(r: ReactorUiState) = when {
    r.isBroken -> Color(0xFFB71C1C)
    r.isOverheated -> Color(0xFFD84315)
    r.isLowFuel -> Color(0xFFF57F17)
    else -> Color(0xFF2E7D32)
}
fun cardBg(r: ReactorUiState) = when {
    r.isBroken -> Color(0xFFE0E0E0)
    r.isOverheated -> Color(0xFFFFCCBC)
    r.isLowFuel -> Color(0xFFFFF9C4)
    else -> Color(0xFFE8F5E9)
}