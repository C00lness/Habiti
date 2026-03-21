package com.habiti.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.habiti.ti.presentation.TiBanner
import com.habiti.habits.impl.presentation.HabitsScreen
import com.habiti.habits.impl.presentation.HabitsViewModel
import org.koin.compose.koinInject

// в app модуле
@Composable
fun MainScreen() {
    val viewModel: HabitsViewModel = koinInject()
    val tiMessage by viewModel.tiMessage.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TiBanner(
            message = tiMessage,
            onDismiss = { viewModel.clearTiMessage() }
        )
        Box(modifier = Modifier.weight(1f)) {
            HabitsScreen(viewModel)
        }
    }
}