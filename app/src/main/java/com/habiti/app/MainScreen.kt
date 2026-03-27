package com.habiti.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.habiti.core.ai.UserPreferences
import com.habiti.ti.presentation.MentorSettingsScreen
import com.habiti.ti.presentation.OnboardingScreen
import com.habiti.ti.presentation.TiBanner
import com.habiti.habits.impl.presentation.HabitsScreen
import com.habiti.habits.impl.presentation.HabitsViewModel
import com.habiti.ti.data.MentorPreferences
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: HabitsViewModel = koinInject()
    val tiMessage by viewModel.tiMessage.collectAsState()

    // Настройки пользователя
    var userPrefs by remember { mutableStateOf<UserPreferences?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    // Загружаем настройки при старте
    LaunchedEffect(Unit) {
        MentorPreferences.getUserPreferences(context).collect { prefs ->
            userPrefs = prefs
        }
    }

    // Если настройки ещё не загружены — показываем загрузку
    if (userPrefs == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Если онбординг не пройден — показываем экран выбора
    if (!userPrefs!!.isOnboardingCompleted) {
        OnboardingScreen(
            onComplete = { prefs ->
                scope.launch {
                    MentorPreferences.saveUserPreferences(context, prefs)
                    userPrefs = prefs
                }
            }
        )
        return
    }

    // Основной экран
    if (showSettings) {
        MentorSettingsScreen(
            currentPrefs = userPrefs!!,
            onSave = { newPrefs ->
                scope.launch {
                    MentorPreferences.saveUserPreferences(context, newPrefs)
                    userPrefs = newPrefs
                    showSettings = false
                }
            },
            onBack = { showSettings = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Habiti") },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки")
                        }
                    }
                )
            }
        ) { paddingValues ->
            // 👇 Используем paddingValues
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Основной контент
                HabitsScreen(viewModel)

                // Баннер с наставником
                AnimatedVisibility(
                    visible = tiMessage != null,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    // 👇 Исправлено: wrap в Column с fillMaxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.33f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            TiBanner(
                                message = tiMessage,
                                mentorType = userPrefs!!.mentorType,
                                onDismiss = { viewModel.clearTiMessage() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}