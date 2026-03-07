package com.habiti.habits.impl.presentation

import com.habiti.habits.impl.domain.Habit

sealed class HabitsUiState {
    object Loading : HabitsUiState()
    data class Success(val habits: List<Habit>) : HabitsUiState()
    data class Error(val message: String) : HabitsUiState()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}