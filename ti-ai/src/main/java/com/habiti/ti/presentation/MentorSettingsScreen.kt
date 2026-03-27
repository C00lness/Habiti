package com.habiti.ti.presentation

import androidx.compose.material.icons.filled.Check

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorSettingsScreen(
    currentPrefs: UserPreferences,
    onSave: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentPrefs.mentorType) }
    var name by remember { mutableStateOf(currentPrefs.mentorName) }
    var showPreview by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки наставника") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Превью текущего наставника
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Здесь будет анимация
                    MentorAvatar(
                        mentorType = selectedType,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (name.isNotBlank()) name else "Наставник",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (selectedType == MentorType.MALE) "Мужчина" else "Женщина",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Выбор пола
            Text("Выберите пол наставника", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = selectedType == MentorType.MALE,
                    onClick = { selectedType = MentorType.MALE },
                    label = { Text("Мужчина") },
                    leadingIcon = if (selectedType == MentorType.MALE) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
                FilterChip(
                    selected = selectedType == MentorType.FEMALE,
                    onClick = { selectedType = MentorType.FEMALE },
                    label = { Text("Женщина") },
                    leadingIcon = if (selectedType == MentorType.FEMALE) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }

            // Имя
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя наставника") },
                placeholder = { Text("Наставник") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
                Button(
                    onClick = {
                        onSave(
                            UserPreferences(
                                mentorType = selectedType,
                                mentorName = name.ifBlank { "Наставник" },
                                isOnboardingCompleted = true
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}