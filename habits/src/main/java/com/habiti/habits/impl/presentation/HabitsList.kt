package com.habiti.habits.impl.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.habiti.habits.impl.cpp.HabitCubeScreen
import com.habiti.habits.impl.domain.Habit

@Composable
fun HabitsList(
    habits: List<Habit>,
    onHabitClick: (String) -> Unit,
    onHabitChecked: (String, Boolean) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onAnalyze: (String) -> Unit,
    correlation:(String) -> Double,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    if (showDeleteDialog && habitToDelete != null) {
        DeleteConfirmationDialog(
            habitName = habitToDelete!!.name,
            onConfirm = {
                onDeleteHabit(habitToDelete!!)
                habitToDelete = null
                showDeleteDialog = false
            },
            onDismiss = {
                habitToDelete = null
                showDeleteDialog = false
            }
        )
    }

    when (habits.size) {
        1 -> SingleHabitLayout(
            habit = habits[0],
            onHabitClick = onHabitClick,
            onHabitChecked = onHabitChecked,
            onDeleteHabit = onDeleteHabit,
            onEditHabit = onEditHabit,
            onAnalyze = onAnalyze,
            correlation = correlation,
            modifier = modifier
        )
        else -> GridHabitsLayout(
            habits = habits,
            onHabitClick = onHabitClick,
            onHabitChecked = onHabitChecked,
            onDeleteHabit = onDeleteHabit,
            onEditHabit = onEditHabit,
            onAnalyze = onAnalyze,
            correlation = correlation,
            modifier = modifier
        )
    }
}