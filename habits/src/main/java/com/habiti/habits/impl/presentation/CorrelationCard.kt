package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun CorrelationCard(
    correlation: Double,
    modifier: Modifier = Modifier
) {
    // Определяем текст и цвет на основе значения корреляции
    val (label, color) = when {
        correlation >= 0.7 -> "Сильная положительная связь 📈" to Color.Green
        correlation >= 0.3 -> "Средняя положительная связь 📊" to Color(0xFFFFA500) // Оранжевый
        correlation > -0.3 -> "Связи нет ⚖️" to Color.Gray
        correlation > -0.7 -> "Средняя отрицательная связь 📉" to Color(0xFFFFA500)
        else -> "Сильная отрицательная связь 📉" to Color.Red
    }

    Card(
        modifier = modifier
            .width(300.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Стабильность привычки",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = String.format("%.2f", correlation),
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}