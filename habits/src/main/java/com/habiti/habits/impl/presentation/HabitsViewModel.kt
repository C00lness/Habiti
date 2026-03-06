package com.habiti.habits.impl.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.habiti.habits.impl.domain.Habit

class HabitsViewModel : ViewModel() {
    var uiState by mutableStateOf<HabitsUiState>(HabitsUiState.Loading)
        private set

    init {
        loadHabits()
    }
    fun loadHabits() {
        uiState = HabitsUiState.Success(
            listOf(
                Habit("1", "Зарядка", "💪", 0xFF4CAF50, 5, 30, 3),
                Habit("2", "Чтение", "📚", 0xFF2196F3, 12, 20, 7),
                Habit("3", "Вода", "💧", 0xFF00BCD4, 18, 30, 14)
            )
        )
    }
}