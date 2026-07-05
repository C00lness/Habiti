package com.habiti.ti.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.UserPreferences
import com.habiti.ti.R
import com.habiti.ti.mentor.PromoPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorSettingsScreen(
    currentPrefs: UserPreferences,
    onSave: (UserPreferences) -> Unit,
    onBack: () -> Unit,
    maxStreak: Int = 0
) {
    val context = LocalContext.current
    val promoPrefs = remember { PromoPreferences(context) }
    val isMrStrickUnlocked = promoPrefs.isMrStrickUnlocked
    val isDancingWomanUnlocked = promoPrefs.isDancingWomanUnlocked

    var selectedType by remember { mutableStateOf(currentPrefs.mentorType) }
    var name by remember { mutableStateOf(currentPrefs.mentorName) }

    // Состояния для промокода
    var promoCode by remember { mutableStateOf("") }
    var promoMessage by remember { mutableStateOf<String?>(null) }
    val defaultMentorName = stringResource(R.string.mentor_name_default)
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
                .padding(16.dp).verticalScroll(rememberScrollState()),

            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Блок с Мисс Привычка
            if (isDancingWomanUnlocked) {
                // ... показываем карточку выбора (как раньше)
            } else {
                // 👇 Показываем прогресс до разблокировки
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "💃 Мисс Привычка",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Выполняйте любую привычку $maxStreak/5 дней подряд",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                        progress = { (maxStreak.toFloat() / 5f).coerceIn(0f, 1f) },
                        modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                        if (maxStreak >= 5) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✅ Разблокирована!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ========== БЛОК ПРОМОКОДА ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Активация промокода",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Если Мистер Стрик уже разблокирован — показываем сообщение
                    if (isMrStrickUnlocked) {
                        Text(
                            text = "✅ Мистер Стрик уже разблокирован!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        OutlinedTextField(
                            value = promoCode,
                            onValueChange = { promoCode = it.uppercase() },
                            label = { Text("Введите промокод") },
                            placeholder = { Text("BOXER100") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (promoCode == "BOXER100") {
                                    promoPrefs.isMrStrickUnlocked = true
                                    promoMessage = "Промокод активирован! Мистер Стрик добавлен в выбор наставников."
                                } else {
                                    promoMessage = "Неверный промокод"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = promoCode.isNotBlank()
                        ) {
                            Text("Активировать")
                        }

                        promoMessage?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (it.contains("активирован"))
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            // ========== КОНЕЦ БЛОКА ПРОМОКОДА ==========
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
                        text = when (selectedType) {
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

            // Ряд с выбором наставника (добавляем Мистера Стрика, если разблокирован)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isMrStrickUnlocked) {
                    FilterChip(
                        selected = selectedType == MentorType.MR_STRICK,
                        onClick = { selectedType = MentorType.MR_STRICK },
                        label = { Text("Мистер Стрик") },
                        leadingIcon = if (selectedType == MentorType.MR_STRICK) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
                if (isDancingWomanUnlocked) {
                    FilterChip(
                        selected = selectedType == MentorType.DANCING_WOMAN,
                        onClick = { selectedType = MentorType.DANCING_WOMAN },
                        label = { Text("Мисс Привычка") },
                        leadingIcon = if (selectedType == MentorType.DANCING_WOMAN) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }


            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.mentor_name_default)) },
                placeholder = { Text(stringResource(R.string.mentor_name_default)) },
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
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        val finalName = when {
                            selectedType == MentorType.MR_STRICK -> "Мистер Стрик"
                            name.isNotBlank() -> name
                            else -> defaultMentorName
                        }
                        onSave(
                            UserPreferences(
                                mentorType = selectedType,
                                mentorName = finalName,
                                isOnboardingCompleted = true
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}