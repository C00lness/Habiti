package com.habiti.habits.impl.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habiti.habits.impl.domain.Habit
import org.koin.compose.koinInject

@Composable
fun HabitsScreen() {
    val viewModel: HabitsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    when (uiState) {
        is HabitsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HabitsUiState.Success -> {
            val habits = (uiState as HabitsUiState.Success).habits
            HabitsList(
                habits = habits,
                onHabitClick = { viewModel.onHabitClick(it) },
                onHabitChecked = { id, checked ->
                    viewModel.onHabitChecked(id, checked)
                },
                onDeleteHabit = { viewModel.onDeleteHabit(it) }
            )
        }
        is HabitsUiState.Error -> {
            val message = (uiState as HabitsUiState.Error).message
            ErrorScreen(message)
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
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
            Row(
                modifier = Modifier.weight(1f).clickable { onClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(habit.icon, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Стрик: ${habit.streak} дней",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${habit.currentCount}/${habit.targetCount}")
                    LinearProgressIndicator(
                        progress = habit.currentCount.toFloat() / habit.targetCount,
                        modifier = Modifier.width(60.dp)
                    )
                }

                Checkbox(
                    checked = habit.isCompletedToday,
                    onCheckedChange = {
                        onCheckedChange(!habit.isCompletedToday)
                    }
                )
            }
        }
    }
}

@Composable
fun HabitsList(
    habits: List<Habit>,
    onHabitClick: (String) -> Unit,
    onHabitChecked: (String, Boolean) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(habits, key = { it.id }) { habit ->
            HabitCard(
                habit = habit,
                onClick = { onHabitClick(habit.id) },
                onCheckedChange = { checked -> onHabitChecked(habit.id, checked) },
                onDelete = { onDeleteHabit(habit) }
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ошибка: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
