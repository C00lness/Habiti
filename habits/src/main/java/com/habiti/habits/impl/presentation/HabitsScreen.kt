package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habiti.habits.impl.domain.Habit
@Composable
fun HabitsScreen() {
    val viewModel: HabitsViewModel = viewModel()

    when (val state = viewModel.uiState) {
        is HabitsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HabitsUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.habits) { habit ->
                    HabitCard(habit)
                }
            }
        }
    }
}

@Composable
fun HabitCard(habit: Habit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(habit.color).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.icon, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                    Text("Стрик: ${habit.streak} дней",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${habit.current}/${habit.target}")
                LinearProgressIndicator(
                    progress = habit.current.toFloat() / habit.target,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}
