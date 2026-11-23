package com.example.androidcourse

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReactorListScreen(
    state: ReactorNetworkUiState,
    onItemClick: (Int) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit,
    onRetryWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Управление генераторами", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Готовы к работе: ${state.activeCount} из ${state.reactors.size} | Всего энергии: ${state.totalEnergy} МВт·ч",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        WeatherStatusCard(
            weatherState = state.weatherState,
            onRefresh = onRetryWeather
        )
        Spacer(modifier = Modifier.height(10.dp))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(reactor.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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

@Composable
private fun WeatherStatusCard(
    weatherState: WeatherUiState,
    onRefresh: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when (weatherState) {
                WeatherUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Загрузка погоды на станции...", fontSize = 13.sp)
                    }
                }

                is WeatherUiState.Content -> {
                    val w = weatherState.weather
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Снаружи: ${w.temperatureC}°C • Ветер: ${w.windSpeedKmh} км/ч",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Эффективность охлаждения: -${w.coolingPower}°C",
                            fontSize = 12.sp,
                            color = Color(0xFF1565C0)
                        )
                    }
                    OutlinedButton(onClick = onRefresh) {
                        Text("Обновить", fontSize = 11.sp)
                    }
                }

                is WeatherUiState.Error -> {
                    Text(
                        text = weatherState.message,
                        fontSize = 12.sp,
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.weight(1f)
                    )
                    if (weatherState.canRetry) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onRefresh) {
                            Text("Повторить", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

fun statusText(r: Reactor): String {
    if (r.isBroken) return "Авария: Разрушен"
    val warnings = buildList {
        if (r.isOverheated) add("Перегрев")
        if (r.isLowFuel) add("Мало топлива")
    }
    return if (warnings.isEmpty()) "Штатный режим" else warnings.joinToString(" • ")
}

fun statusColor(r: Reactor) = when {
    r.isBroken -> Color(0xFFB71C1C)
    r.isOverheated -> Color(0xFFD84315)
    r.isLowFuel -> Color(0xFFF57F17)
    else -> Color(0xFF2E7D32)
}

fun cardBg(r: Reactor) = when {
    r.isBroken -> Color(0xFFE0E0E0)
    r.isOverheated -> Color(0xFFFFCCBC)
    r.isLowFuel -> Color(0xFFFFF9C4)
    else -> Color(0xFFE8F5E9)
}