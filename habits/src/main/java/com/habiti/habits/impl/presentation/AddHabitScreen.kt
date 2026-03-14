package com.habiti.habits.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habiti.habits.impl.R
import com.habiti.habits.impl.domain.Habit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onHabitAdded: () -> Unit,
    onCancel: () -> Unit,
    viewModel: HabitsViewModel = viewModel(),
    habitToEdit: Habit? = null
) {
    val targetCountDefault = stringResource(R.string.target_count_default)
    val isEditing = habitToEdit != null
    var name by remember {mutableStateOf(habitToEdit?.name ?: "") }
    var targetCount by remember { mutableStateOf(habitToEdit?.targetCount?.toString() ?: targetCountDefault) }
    var icon by remember { mutableStateOf(habitToEdit?.icon ?: "💪") }
    var selectedColor by remember {  mutableStateOf(habitToEdit?.color ?: 0xFF4CAF50) }

    val coroutineScope = rememberCoroutineScope()
    val icons = listOf("💪", "📚", "💧", "🧘", "🚶", "🏋️", "🥗", "😴", "🎯", "✍️")
    val colors = listOf(
        0xFF4CAF50 to stringResource(R.string.color_green),
        0xFF2196F3 to stringResource(R.string.color_blue),
        0xFF00BCD4 to stringResource(R.string.color_cyan),
        0xFFFF9800 to stringResource(R.string.color_orange),
        0xFF9C27B0 to stringResource(R.string.color_purple),
        0xFFE91E63 to stringResource(R.string.color_pink)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_habit_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Text("✕")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                val habit = Habit(
                                    id = habitToEdit?.id ?: "0",
                                    name = name,
                                    description = null,
                                    icon = icon,
                                    color = selectedColor,
                                    targetCount = targetCount.toIntOrNull() ?: 30,
                                    currentCount = 0,
                                    streak = 0,
                                    maxStreak = 0,
                                    createdAtMillis = System.currentTimeMillis(),
                                    updatedAtMillis = System.currentTimeMillis(),
                                    isArchived = false,
                                    lastCompletedDate = null,
                                    reminderTime = null,
                                    reminderDays = null
                                )
                                if (habitToEdit != null) {
                                    viewModel.updateHabit(habit)  // ← вызываем update для редактирования
                                } else {
                                    viewModel.addNewHabit(habit)  // ← add для новой
                                }
                                onHabitAdded()
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.habit_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            // Иконка (выбор из списка)
            Text(stringResource(R.string.choose_icon), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.chunked(5).forEach { rowIcons ->
                    Column {
                        rowIcons.forEach { emoji ->
                            FilterChip(
                                selected = icon == emoji,
                                onClick = { icon = emoji },
                                label = { Text(emoji) }
                            )
                        }
                    }
                }
            }

            // Цвет
            Text(stringResource(R.string.choose_color), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { (color, name) ->
                    FilterChip(
                        selected = selectedColor == color,
                        onClick = { selectedColor = color },
                        label = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(color))
                            )
                        },
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            // Цель (количество дней)
            OutlinedTextField(
                value = targetCount,
                onValueChange = { targetCount = it },
                label = { Text(stringResource(R.string.target_days)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = targetCount.toIntOrNull() == null
            )
        }
    }
}