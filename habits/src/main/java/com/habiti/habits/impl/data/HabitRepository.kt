package com.habiti.habits.impl.data

import android.util.Log
import com.habiti.habits.impl.common.Converters
import com.habiti.habits.impl.domain.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val database: HabitsDb) {
    private val dao = database.habitDao()
    private fun HabitEntity.toDomain(): Habit {
        return Habit(
            id = id.toString(),
            name = name,
            description = description,
            icon = icon,
            color = color,
            targetCount = targetCount,
            currentCount = currentCount,
            streak = streak,
            maxStreak = maxStreak,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
            isArchived = isArchived,
            reminderTime = reminderTime,
            reminderDays = Converters.toDaysList(reminderDays),
            lastCompletedDate = this.lastCompletedDate
        )
    }

    private fun Habit.toEntity(): HabitEntity {
        return HabitEntity(
            id = id.toLongOrNull() ?: 0,
            name = name,
            description = description,
            icon = icon,
            color = color,
            targetCount = targetCount,
            currentCount = currentCount,
            streak = streak,
            maxStreak = maxStreak,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
            isArchived = isArchived,
            reminderTime = reminderTime,
            reminderDays = Converters.fromDaysList(reminderDays),
            lastCompletedDate = lastCompletedDate
        )
    }

    // Все привычки (Flow обновляется автоматически)
    fun getAllHabits(): Flow<List<Habit>> {
        return dao.getAllHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Поиск
    fun searchHabits(query: String): Flow<List<Habit>> {
        return dao.searchHabits(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Добавить привычку
    suspend fun insertHabit(habit: Habit): Long {
        return dao.insertHabit(habit.toEntity())
    }

    // Обновить привычку
    suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(habit.toEntity())
    }

    // Удалить привычку
    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit.toEntity())
    }

    // Отметить выполнение
    suspend fun incrementProgress(habitId: String) {
        val id = habitId.toLongOrNull() ?: return
        dao.incrementProgress(id)
    }

    suspend fun updateLastCompletedDate(habitId: String, date: Long) {
        val id = habitId.toLongOrNull() ?: return
        if (id != null)
            dao.updateLastCompletedDate(id, date)
    }

    fun updateLastCompletedDateSync(habitId: String, date: Long) {
        val id = habitId.toLongOrNull() ?: return
        dao.updateLastCompletedDateSync(id, date)
    }
}