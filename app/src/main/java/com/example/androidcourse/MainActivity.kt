package com.example.androidcourse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val reactorState = remember { ReactorState(name = "Генератор «Сильвер-1»") }

                    ReactorScreen(
                        reactor = reactorState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

class ReactorState(
    val name: String,
    val maxFuel: Int = 100,
    val maxTemp: Int = 120,
    val maxHull: Int = 100
) {
    var fuel by mutableIntStateOf(80)
        private set
    var temperature by mutableIntStateOf(30)
        private set
    var hull by mutableIntStateOf(100)
        private set
    var totalEnergy by mutableIntStateOf(0)
        private set

    val isBroken: Boolean
        get() = hull <= 0
    val isOverheated: Boolean
        get() = !isBroken && temperature >= 85
    val isSafeForMaintenance: Boolean
        get() = !isBroken && temperature <= 50
    val canGenerate: Boolean
        get() = !isBroken && fuel >= 20 && temperature <= 105

    fun generatePower() {
        if (!canGenerate) return
        fuel = (fuel - 20).coerceAtLeast(0)
        temperature = (temperature + 25).coerceAtMost(maxTemp)
        totalEnergy += 50
        if (temperature >= 85) {
            hull = (hull - 35).coerceAtLeast(0)
        }
    }

    val canCoolDown: Boolean
        get() = !isBroken && temperature > 20 && fuel >= 10

    fun coolDown() {
        if (!canCoolDown) return
        fuel = (fuel - 10).coerceAtLeast(0)
        temperature = (temperature - 35).coerceAtLeast(20)
    }

    val canRefuel: Boolean
        get() = isSafeForMaintenance && fuel < maxFuel

    fun refuel() {
        if (!canRefuel) return
        fuel = (fuel + 40).coerceAtMost(maxFuel)
    }

    val canRepair: Boolean
        get() = isSafeForMaintenance && hull < maxHull

    fun repairHull() {
        if (!canRepair) return
        hull = (hull + 30).coerceAtMost(maxHull)
    }

    fun reset() {
        fuel = 80
        temperature = 30
        hull = 100
        totalEnergy = 0
    }
}

@Composable
fun ReactorScreen(reactor: ReactorState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor(reactor)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.generator_img),
                    contentDescription = "Генератор",
                    modifier = Modifier
                        .size(130.dp)
                        .alpha(if (reactor.isBroken) 0.4f else 1.0f),
                    colorFilter = if (reactor.isBroken) ColorFilter.tint(Color.Gray) else null
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reactor.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText(reactor),
                    fontWeight = FontWeight.Medium,
                    color = statusTextColor(reactor),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Выработано энергии: ${reactor.totalEnergy} МВт·ч",
                    fontSize = 16.sp
                )
            }
        }

        if (reactor.isOverheated) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ВНИМАНИЕ: Перегрев генератора! Дальнейший форсаж повредит обшивку. Заправка и ремонт заблокированы до охлаждения (<= 50°C).",
                    color = Color(0xFFB71C1C),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        } else if (!reactor.isBroken && !reactor.isSafeForMaintenance) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Для заправки и ремонта охладите генератор до 50°C",
                color = Color(0xFFEF6C00),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Целостность корпуса: ${reactor.hull} / ${reactor.maxHull}%")
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { reactor.hull.toFloat() / reactor.maxHull },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = barColor(reactor.hull, reactor.maxHull)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Топливо: ${reactor.fuel} / ${reactor.maxFuel} ед.")
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { reactor.fuel.toFloat() / reactor.maxFuel },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = barColor(reactor.fuel, reactor.maxFuel)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Температура генератора: ${reactor.temperature}°C (макс. ${reactor.maxTemp}°C)")
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { reactor.temperature.toFloat() / reactor.maxTemp },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = tempBarColor(reactor.temperature)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { reactor.generatePower() },
                enabled = reactor.canGenerate,
                modifier = Modifier.weight(1f)
            ) {
                Text("Форсаж (+50 МВт)")
            }
            Button(
                onClick = { reactor.coolDown() },
                enabled = reactor.canCoolDown,
                modifier = Modifier.weight(1f)
            ) {
                Text("Охладить (-35°C)")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { reactor.refuel() },
                enabled = reactor.canRefuel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Заправить (+40)")
            }

            Button(
                onClick = { reactor.repairHull() },
                enabled = reactor.canRepair,
                modifier = Modifier.weight(1f)
            ) {
                Text("Ремонт (+30%)")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { reactor.reset() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (reactor.isBroken) "Аварийный перезапуск генератора"
                else "Сбросить в исходное состояние"
            )
        }
    }
}

fun statusText(reactor: ReactorState): String = when {
    reactor.isBroken -> "АВАРИЯ: Разрушение корпуса! Работа заблокирована"
    reactor.isOverheated -> "КРИТИЧЕСКИЙ РЕЖИМ: Опасный перегрев генератора!"
    reactor.fuel < 20 -> "ОЖИДАНИЕ: Недостаточно топлива для генерации"
    else -> "ШТАТНЫЙ РЕЖИМ: Системы в норме"
}

fun statusTextColor(reactor: ReactorState): Color = when {
    reactor.isBroken -> Color(0xFFB71C1C)
    reactor.isOverheated -> Color(0xFFD84315)
    reactor.fuel < 20 -> Color(0xFFF57F17)
    else -> Color(0xFF2E7D32)
}

fun cardBackgroundColor(reactor: ReactorState): Color = when {
    reactor.isBroken -> Color(0xFFE0E0E0)
    reactor.isOverheated -> Color(0xFFFFCCBC)
    reactor.fuel < 20 -> Color(0xFFFFF9C4)
    else -> Color(0xFFE8F5E9)
}

fun barColor(value: Int, max: Int): Color = when {
    value * 100 / max <= 30 -> Color(0xFFE53935)
    value * 100 / max <= 60 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

fun tempBarColor(temp: Int): Color = when {
    temp >= 85 -> Color(0xFFE53935)
    temp > 50 -> Color(0xFFFB8C00)
    else -> Color(0xFF1E88E5)
}