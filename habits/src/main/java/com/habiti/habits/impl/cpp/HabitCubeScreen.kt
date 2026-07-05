package com.habiti.habits.impl.cpp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habiti.habits.impl.domain.Habit

@Composable
fun HabitCubeScreen(
    habit: Habit,
    onClose: () -> Unit
) {
    val progress = remember(habit.streak, habit.targetCount) {
        if (habit.targetCount > 0) {
            (habit.streak.toFloat() / habit.targetCount).coerceIn(0f, 1f)
        } else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Название привычки
            Text(
                text = habit.name,
                color = Color.White,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Большой куб
            AndroidView(
                factory = { context ->
                    HabitCubeView(context).apply {
                        updateProgress(progress)
                    }
                },
                update = { view ->
                    view.updateProgress(progress)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Прогресс
            Text(
                text = "Прогресс: ${(progress * 100).toInt()}%",
                color = Color.White,
                fontSize = 18.sp
            )

            Text(
                text = "Стрик: ${habit.streak}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка закрыть
            Button(onClick = onClose) {
                Text("Закрыть")
            }
        }
    }
}