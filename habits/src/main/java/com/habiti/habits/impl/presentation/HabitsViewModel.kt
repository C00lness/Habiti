package com.habiti.habits.impl.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habiti.habits.impl.data.HabitRepository
import com.habiti.habits.impl.domain.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HabitsViewModel( private val repository: HabitRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HabitsUiState>(HabitsUiState.Loading)
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    init {
        observeAllHabits()
    }

    private fun observeAllHabits() {
        repository.getAllHabits()
            .onEach { habits ->
                _uiState.value = HabitsUiState.Success(habits)
            }
            .launchIn(viewModelScope)
    }

    private fun searchHabits(query: String) {
        repository.searchHabits(query)
            .onEach { habits ->
                _uiState.value = HabitsUiState.Success(habits)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            observeAllHabits()
        } else {
            searchHabits(query)
        }
    }

    fun onAddHabitClick() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newHabit = Habit(
                id = "0", // будет заменено при вставке
                name = "Новая привычка",
                description = null,
                icon = "✨",
                color = 0xFF6B4EFF,
                targetCount = 30,
                currentCount = 0,
                streak = 0,
                maxStreak = 0,
                createdAtMillis = now,
                updatedAtMillis = now,
                isArchived = false,
                reminderTime = null,
                reminderDays = null
            )
            repository.insertHabit(newHabit)
        }
    }

    // Отметка выполнения привычки
    fun onHabitChecked(habitId: String, checked: Boolean) {
        if (checked) {
            viewModelScope.launch {
                repository.incrementProgress(habitId)
                repository.updateLastCompletedDate(habitId, System.currentTimeMillis())
            }
        }
    }

    // Удаление привычки
    fun onDeleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Клик по привычке (открыть детали)
    fun onHabitClick(habitId: String) {
        // TODO: реализовать навигацию
    }
}