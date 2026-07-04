package com.habiti.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

@Composable
fun HabitCubeScreen() {
    var progress by remember { mutableStateOf(0.0f) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 👇 СОХРАНЯЕМ ССЫЛКУ НА VIEW
        var cubeView by remember { mutableStateOf<HabitCubeView?>(null) }

        AndroidView(
            factory = { context ->
                HabitCubeView(context).also {
                    cubeView = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(1500.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Прогресс: ${(progress * 100).toInt()}%")

        Slider(
            value = progress,
            onValueChange = { newProgress ->
                progress = newProgress
                cubeView?.updateProgress(newProgress) // 👈 ОБНОВЛЯЕМ ПРИ ИЗМЕНЕНИИ
            },
            modifier = Modifier.padding(16.dp)
        )

        Button(onClick = {
            progress = (progress + 0.1f).coerceAtMost(1.0f)
            cubeView?.updateProgress(progress) // 👈 ОБНОВЛЯЕМ ПРИ КЛИКЕ
        }) {
            Text("+10%")
        }
    }
}