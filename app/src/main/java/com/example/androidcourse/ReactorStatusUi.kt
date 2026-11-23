package com.example.androidcourse

import androidx.compose.ui.graphics.Color

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