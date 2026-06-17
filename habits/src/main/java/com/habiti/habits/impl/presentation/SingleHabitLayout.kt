package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habiti.habits.impl.domain.Habit

@Composable
fun SingleHabitLayout(
    habit: Habit,
    onHabitClick: (String) -> Unit,
    onHabitChecked: (String, Boolean) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onAnalyze: (String) -> Unit,
    correlation: (String) -> Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        HabitCard(
            habit = habit,
            onAnalyze = {onAnalyze(habit.id)},
            onClick = { onHabitClick(habit.id) },
            onCheckedChange = { checked -> onHabitChecked(habit.id, checked) },
            onDelete = { onDeleteHabit(habit) },
            onEdit = { onEditHabit(habit) },
            correlation = correlation(habit.id),
            modifier = Modifier.width(300.dp).aspectRatio(1f).padding(4.dp)
        )
    }
}