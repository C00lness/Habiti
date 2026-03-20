package com.habiti.habits.impl.presentation

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

    private val _navigateToAdd = MutableStateFlow(false)
    val navigateToAdd: StateFlow<Boolean> = _navigateToAdd.asStateFlow()

    private val _habitToEdit = MutableStateFlow<Habit?>(null)
    val habitToEdit: StateFlow<Habit?> = _habitToEdit.asStateFlow()

    fun getHabitName(habitId: String): String? {
        val state = _uiState.value
        return if (state is HabitsUiState.Success) {
            state.habits.find { it.id == habitId }?.name
        } else null
    }

    fun onEditHabit(habit: Habit) {
        _habitToEdit.value = habit
    }

    fun clearEditHabit() {
        _habitToEdit.value = null
    }

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

    fun onAddHabitClick(id: String, name: String, description: String, icon: String, isArchive: Boolean) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newHabit = Habit(
                id = id,
                name = name,
                description = description,
                icon = icon,
                color = 0xFF6B4EFF,
                targetCount = 30,
                currentCount = 0,
                streak = 0,
                maxStreak = 0,
                createdAtMillis = now,
                updatedAtMillis = now,
                isArchived = isArchive,
                reminderTime = null,
                reminderDays = null,
                reminderEnabled = false,
                reminderHour = null,
                reminderMinute = null
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

    fun onAddHabitClick() {
        _navigateToAdd.value = true
    }

    fun onAddScreenClosed() {
        _navigateToAdd.value = false
    }

    fun addNewHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }
}