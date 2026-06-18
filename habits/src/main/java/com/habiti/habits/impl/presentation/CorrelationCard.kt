package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CorrelationCard(
    correlation: Double,
    modifier: Modifier = Modifier
) {
    val (label, color) = when {
        correlation >= 0.7 -> "Сильная 📈" to Color.Green
        correlation >= 0.3 -> "Средняя 📊" to Color(0xFFFFA500)
        correlation > -0.3 -> "Нет ⚖️" to Color.Gray
        correlation > -0.7 -> "Средняя 📉" to Color(0xFFFFA500)
        else -> "Сильная 📉" to Color.Red
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Стабильность.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = String.format("%.2f", correlation),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 14.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = color
            )
        }
    }
}