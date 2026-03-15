package com.habiti.habits.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import com.habiti.habits.impl.R
import com.habiti.habits.impl.domain.Habit
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onHabitAdded: () -> Unit,
    onCancel: () -> Unit,
    viewModel: HabitsViewModel,
    habitToEdit: Habit? = null
) {
    val isEditing = habitToEdit != null

    // Состояния
    var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var targetCount by remember { mutableStateOf(habitToEdit?.targetCount?.toString() ?: "30") }
    var icon by remember { mutableStateOf(habitToEdit?.icon ?: "💪") }
    var selectedColor by remember { mutableStateOf(habitToEdit?.color ?: 0xFF4CAF50) }

    // Состояния для напоминания
    var reminderEnabled by remember { mutableStateOf(habitToEdit?.reminderEnabled ?: false) }
    var reminderHour by remember { mutableStateOf(habitToEdit?.reminderHour ?: 9) }
    var reminderMinute by remember { mutableStateOf(habitToEdit?.reminderMinute ?: 0) }
    var reminderDays by remember { mutableStateOf(habitToEdit?.reminderDays) }

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
                title = {
                    Text(if (isEditing)
                        stringResource(R.string.edit_habit_title)
                    else stringResource(R.string.new_habit_title)
                    )
                },
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
                                    currentCount = habitToEdit?.currentCount ?: 0,
                                    streak = habitToEdit?.streak ?: 0,
                                    maxStreak = habitToEdit?.maxStreak ?: 0,
                                    createdAtMillis = habitToEdit?.createdAtMillis ?: System.currentTimeMillis(),
                                    updatedAtMillis = System.currentTimeMillis(),
                                    isArchived = habitToEdit?.isArchived ?: false,
                                    lastCompletedDate = habitToEdit?.lastCompletedDate,
                                    reminderEnabled = reminderEnabled,
                                    reminderHour = if (reminderEnabled) reminderHour else null,
                                    reminderMinute = if (reminderEnabled) reminderMinute else null,
                                    reminderDays = if (reminderEnabled) reminderDays else null
                                )

                                if (habitToEdit != null) {
                                    viewModel.updateHabit(habit)
                                } else {
                                    viewModel.addNewHabit(habit)
                                }
                                onHabitAdded()
                            }
                        },
                        enabled = name.isNotBlank() && targetCount.toIntOrNull() != null
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Название привычки
            item {
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
            }

            // Иконки
            item {
                Text(stringResource(R.string.choose_icon), style = MaterialTheme.typography.titleSmall)
            }

            item {
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
            }

            // Цвета
            item {
                Text(stringResource(R.string.choose_color), style = MaterialTheme.typography.titleSmall)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (color, _) ->
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
            }

            // Цель
            item {
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

            // Напоминание
            item {
                ReminderSection(
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    onReminderEnabledChange = { reminderEnabled = it },
                    onTimeChanged = { hour, minute ->
                        reminderHour = hour
                        reminderMinute = minute
                    }
                )
            }

            // Отступ снизу
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ReminderSection_new(
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onReminderEnabledChange: (Boolean) -> Unit,
    onTimeChanged: (Int, Int) -> Unit
) {
    Column {
        // Заголовок и переключатель
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.reminder),
                style = MaterialTheme.typography.titleMedium
            )
            androidx.compose.material3.Switch(
                checked = reminderEnabled,
                onCheckedChange = onReminderEnabledChange
            )
        }

        if (reminderEnabled) {
            Spacer(modifier = Modifier.height(12.dp))

            // Поля для ввода времени
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = reminderHour.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { hour ->
                            if (hour in 0..23) {
                                onTimeChanged(hour, reminderMinute)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.reminder_input_hour)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reminderMinute.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { minute ->
                            if (minute in 0..59) {
                                onTimeChanged(reminderHour, minute)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.reminder_input_minute)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Кнопки быстрого выбора
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "08:00" to Pair(8, 0),
                    "09:00" to Pair(9, 0),
                    "18:00" to Pair(18, 0),
                    "20:00" to Pair(20, 0)
                ).forEach { (label, time) ->
                    TextButton(
                        onClick = {
                            onTimeChanged(time.first, time.second)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }

            // Отображение выбранного времени
            Text(
                String.format("Будильник сработает в %02d:%02d", reminderHour, reminderMinute),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}