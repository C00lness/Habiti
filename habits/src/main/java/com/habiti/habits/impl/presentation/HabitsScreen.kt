package com.habiti.habits.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habiti.habits.impl.R
import com.habiti.habits.impl.cpp.HabitCubeScreen

@Composable
fun HabitsScreen(viewModel: HabitsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navigateToAdd by viewModel.navigateToAdd.collectAsState()
    val habitToEdit by viewModel.habitToEdit.collectAsState()

    when {
        navigateToAdd -> {
            AddHabitScreen(
                onHabitAdded = {
                    viewModel.onAddScreenClosed()
                },
                onCancel = {
                    viewModel.onAddScreenClosed()
                },
                viewModel = viewModel
            )
        }
        habitToEdit != null -> {
            AddHabitScreen(
                onHabitAdded = {
                    viewModel.clearEditHabit()
                },
                onCancel = {
                    viewModel.clearEditHabit()
                },
                viewModel = viewModel,
                habitToEdit = habitToEdit
            )
        }
        else -> {
            HabitsListScreen(viewModel, uiState)
        }
    }
}



@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.error, message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}


@Composable
fun HabitsListScreen(
    viewModel: HabitsViewModel,
    uiState: HabitsUiState
) {
    val correlationMap by viewModel.correlationMap.collectAsState()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(
                onClick = { viewModel.onAddHabitClick() }
            ) {
                Text("+")
            }
        }

        when (uiState) {
            is HabitsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HabitsUiState.Success -> {
                HabitsList(
                    habits = uiState.habits,
                    onHabitClick = { viewModel.onHabitClick(it) },
                    onHabitChecked = { id, checked ->
                        viewModel.onHabitChecked(id, checked)
                    },
                    onDeleteHabit = { viewModel.onDeleteHabit(it) },
                    onEditHabit = {viewModel.onEditHabit(it)},
                    onAnalyze = {viewModel.analyzeHabit(it)},
                    correlation = {habitId -> correlationMap[habitId] ?: 0.0}
                )
            }

            else -> {}
        }
    }
}
