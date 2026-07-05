package com.habiti.habits.impl.presentation

import HabitCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habiti.habits.impl.cpp.HabitCubeScreen
import com.habiti.habits.impl.domain.Habit

@Composable
fun GridHabitsLayout(
    habits: List<Habit>,
    onHabitClick: (String) -> Unit,
    onHabitChecked: (String, Boolean) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onAnalyze: (String) -> Unit,
    correlation: (String) -> Double,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(habits, key = { it.id }) { habit ->
            HabitCard(
                habit = habit,
                onAnalyze = { onAnalyze(habit.id) },
                onClick = { onHabitClick(habit.id) },
                onCheckedChange = { checked -> onHabitChecked(habit.id, checked) },
                onDelete = { onDeleteHabit(habit) },
                onEdit = { onEditHabit(habit) },
                correlation = correlation(habit.id),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            )
        }
    }
}