package com.habiti.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habiti.ti.presentation.TiBanner
import com.habiti.habits.impl.presentation.HabitsScreen
import com.habiti.habits.impl.presentation.HabitsViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun MainScreen() {
    val viewModel: HabitsViewModel = koinInject()
    val tiMessage by viewModel.tiMessage.collectAsState()
    LaunchedEffect(Unit) {
        while (true) {
            delay(24 * 60 * 60 * 1000)
            viewModel.checkMissedHabits()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        HabitsScreen(viewModel)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.25f)
                ) {
                    TiBanner(
                        message = tiMessage,
                        onDismiss = { viewModel.clearTiMessage() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}