package com.habiti.habits.impl.presentation

import com.habiti.habits.impl.domain.Habit

sealed class HabitsUiState {
    object Loading : HabitsUiState()
    data class Success(val habits: List<Habit>) : HabitsUiState()
}