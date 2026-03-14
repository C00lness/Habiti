package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habiti.habits.impl.domain.Habit

@Composable
fun HabitsList(
    habits: List<Habit>,
    onHabitClick: (String) -> Unit,
    onHabitChecked: (String, Boolean) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 80.dp,
                start = 16.dp,
                end = 16.dp,
                top = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    onClick = { onHabitClick(habit.id) },
                    onCheckedChange = { checked -> onHabitChecked(habit.id, checked) },
                    onDelete = {
                        habitToDelete = habit
                        showDeleteDialog = true
                    },
                    onEdit = { onEditHabit(habit) }
                )
            }
        }
    }
}