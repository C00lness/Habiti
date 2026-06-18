package com.habiti.habits.impl.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habiti.core.ai.MessageContext
import com.habiti.core.ai.TiMessage
import com.habiti.core.ai.TiMotivator
import com.habiti.habits.impl.common.isMilestoneStreak
import com.habiti.habits.impl.data.HabitHistoryRepository
import com.habiti.habits.impl.data.HabitRepository
import com.habiti.habits.impl.domain.Habit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.example.nativetools.NativeLib

class HabitsViewModel( private val repository: HabitRepository,
                       private val tiMotivator: TiMotivator,
                       private val historyRepository: HabitHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HabitsUiState>(HabitsUiState.Loading)
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _navigateToAdd = MutableStateFlow(false)
    val navigateToAdd: StateFlow<Boolean> = _navigateToAdd.asStateFlow()

    private val _habitToEdit = MutableStateFlow<Habit?>(null)
    val habitToEdit: StateFlow<Habit?> = _habitToEdit.asStateFlow()

    private val _tiMessage = MutableStateFlow<TiMessage?>(null)
    val tiMessage: StateFlow<TiMessage?> = _tiMessage.asStateFlow()
    private val _correlationMap = MutableStateFlow<Map<String, Double?>>(emptyMap())
    val correlationMap: StateFlow<Map<String, Double?>> = _correlationMap.asStateFlow()

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
                val oldHabit = repository.getHabitById(habitId)
                repository.incrementProgress(habitId)
                repository.updateLastCompletedDate(habitId, System.currentTimeMillis())
                historyRepository.addRecord(habitId, true)
                val newHabit = repository.getHabitById(habitId)
                val habitName = newHabit?.name ?: return@launch
                val completedMsg = tiMotivator.getMessage(MessageContext.Completed(habitName))
                _tiMessage.value = completedMsg
                val newStreak = newHabit.streak ?: 0
                val oldStreak = oldHabit?.streak ?: 0
                if (newStreak > oldStreak && isMilestoneStreak(newStreak)) {
                    delay(5000)
                    val streakMsg = tiMotivator.getMessage(MessageContext.Streak(habitName, newStreak))
                    _tiMessage.value = streakMsg
                }
                delay(5000)
                _tiMessage.value = null
            }
        }
    }
    fun onDeleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
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
    fun clearTiMessage() {
        _tiMessage.value = null
    }

    fun checkMissedHabits() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is HabitsUiState.Success) {
                val habits = state.habits

                val missedHabits = habits.filter { !it.isCompletedToday }

                if (missedHabits.isNotEmpty()) {
                    val randomHabit = missedHabits.random()
                    val missedMsg = tiMotivator.getMessage(MessageContext.Missed(randomHabit.name))
                    _tiMessage.value = missedMsg
                    delay(3000)
                    _tiMessage.value = null
                }
            }
        }
    }

    // Вызов нативной корреляции (например, по кнопке)
    fun analyzeHabit(habitId: String) {
        viewModelScope.launch {
            val history = historyRepository.getLastNDays(habitId, 30)
            if (history.isEmpty()) {
                Log.d("JNI_Test", "Нет данных для анализа")
                return@launch
            }

            // Подготовка данных для нативной функции
            val x = history.mapIndexed { index, _ -> index.toDouble() }.toDoubleArray() // дни
            val y = history.map { if (it.second) 1.0 else 0.0 }.toDoubleArray() // выполнение

            val correlation = NativeLib().calculateCorrelation(x, y)
            Log.d("JNI_Test", "Correlation $habitId: $correlation")

            updateCorrelation(habitId)
            // Можно показать в UI
            //_uiState.value = HabitsUiState.Success(/* обновлённый список с метрикой */)
        }
    }

    fun getCorrelationForHabit(habitId: String): Double? {
        return _correlationMap.value[habitId]
    }

    private fun updateCorrelation(habitId: String) {
        viewModelScope.launch {
            val history = historyRepository.getLastNDays(habitId, 30)
            if (history.size < 3) {
                _correlationMap.value = _correlationMap.value.toMutableMap().apply {
                    put(habitId, null)
                }
                return@launch
            }
            val x = history.mapIndexed { index, _ -> index.toDouble() }.toDoubleArray()
            val y = history.map { if (it.second) 1.0 else 0.0 }.toDoubleArray()
            val correlation = NativeLib().calculateCorrelation(x, y)

            // Обновляем карту
            _correlationMap.value = _correlationMap.value.toMutableMap().apply {
                put(habitId, correlation)
            }
        }
    }
}