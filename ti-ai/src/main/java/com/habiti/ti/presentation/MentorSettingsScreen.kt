package com.habiti.ti.presentation

import androidx.compose.material.icons.filled.Check

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.UserPreferences
import com.habiti.ti.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorSettingsScreen(
    currentPrefs: UserPreferences,
    onSave: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentPrefs.mentorType) }
    var name by remember { mutableStateOf(currentPrefs.mentorName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mentor_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                    MentorAvatar(
                        mentorType = selectedType,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (name.isNotBlank()) name else stringResource(R.string.mentor_name_default),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = when (selectedType)
                        {
                            MentorType.MALE -> stringResource(R.string.mentor_man)
                            MentorType.FEMALE -> stringResource(R.string.mentor_woman)
                            MentorType.CAT -> stringResource(R.string.mentor_cat)
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text("Выберите наставника", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = selectedType == MentorType.MALE,
                    onClick = { selectedType = MentorType.MALE },
                    label = { Text(stringResource(R.string.mentor_man)) },
                    leadingIcon = if (selectedType == MentorType.MALE) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
                FilterChip(
                    selected = selectedType == MentorType.FEMALE,
                    onClick = { selectedType = MentorType.FEMALE },
                    label = { Text(stringResource(R.string.mentor_woman)) },
                    leadingIcon = if (selectedType == MentorType.FEMALE) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
                FilterChip(
                    selected = selectedType == MentorType.CAT,
                    onClick = { selectedType = MentorType.CAT },
                    label = { Text(stringResource(R.string.mentor_cat)) },
                    leadingIcon = if (selectedType == MentorType.CAT) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.mentor_name_default)) },
                placeholder = { Text(stringResource(R.string.mentor_name_default)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val mentorName = (stringResource(R.string.mentor_name_default))
            val save = (stringResource(R.string.save))
            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onSave(
                            UserPreferences(
                                mentorType = selectedType,
                                mentorName = name.ifBlank { mentorName },
                                isOnboardingCompleted = true
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(save)
                }
            }
        }
    }
}