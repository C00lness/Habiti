package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habiti.habits.impl.R
@Composable
fun ReminderSection(
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onReminderEnabledChange: (Boolean) -> Unit,
    onTimeChanged: (Int, Int) -> Unit
) {
    // Локальные состояния для полей ввода
    var hourText by remember { mutableStateOf(reminderHour.toString()) }
    var minuteText by remember { mutableStateOf(reminderMinute.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Верхняя строка с переключателем
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.reminder),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (reminderEnabled) {
                        Text(
                            String.format("%02d:%02d", reminderHour, reminderMinute),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderEnabledChange
                )
            }

            // Поля ввода времени (показываем если включено)
            if (reminderEnabled) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Часы
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { newValue ->
                            hourText = newValue.filter { it.isDigit() }
                            hourText.toIntOrNull()?.let { hour ->
                                if (hour in 0..23) {
                                    onTimeChanged(hour, reminderMinute)
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.reminder_hour)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = hourText.toIntOrNull() !in 0..23,
                        supportingText = {
                            if (hourText.toIntOrNull() !in 0..23) {
                                Text((stringResource(R.string.reminder_input_hour)))
                            }
                        }
                    )

                    // Минуты
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { newValue ->
                            minuteText = newValue.filter { it.isDigit() }
                            minuteText.toIntOrNull()?.let { minute ->
                                if (minute in 0..59) {
                                    onTimeChanged(reminderHour, minute)
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.reminder_minute)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = minuteText.toIntOrNull() !in 0..59,
                        supportingText = {
                            if (minuteText.toIntOrNull() !in 0..59) {
                                Text(stringResource(R.string.reminder_input_minute))
                            }
                        }
                    )
                }

                // Кнопки быстрого выбора (опционально)
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
                        OutlinedButton(
                            onClick = {
                                hourText = time.first.toString()
                                minuteText = time.second.toString()
                                onTimeChanged(time.first, time.second)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}