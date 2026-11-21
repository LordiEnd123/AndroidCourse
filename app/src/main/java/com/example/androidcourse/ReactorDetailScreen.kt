package com.example.androidcourse

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReactorDetailScreen(
    state: Reactor,
    onGenerate: () -> Unit,
    onCoolDown: () -> Unit,
    onRefuel: () -> Unit,
    onRepair: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier)
{
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        OutlinedButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("← К списку")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg(state)),
            modifier = Modifier.fillMaxWidth())
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally)
            {
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
                    })
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = statusText(state),
                    fontWeight = FontWeight.Medium,
                    color = statusColor(state),
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Выработано: ${state.totalEnergy} МВт·ч", fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Корпус: ${state.hull} / ${state.maxHull}%")
        LinearProgressIndicator(
            progress = { state.hull.toFloat() / state.maxHull },
            modifier = Modifier.fillMaxWidth().height(10.dp))

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Топливо: ${state.fuel} / ${state.maxFuel} ед.")
        LinearProgressIndicator(
            progress = { state.fuel.toFloat() / state.maxFuel },
            modifier = Modifier.fillMaxWidth().height(10.dp))

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Температура ядра: ${state.temperature}°C (макс. ${state.maxTemp}°C)")
        LinearProgressIndicator(
            progress = { state.temperature.toFloat() / state.maxTemp },
            modifier = Modifier.fillMaxWidth().height(10.dp))

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