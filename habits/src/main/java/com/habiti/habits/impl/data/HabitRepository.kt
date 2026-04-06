package com.habiti.habits.impl.data

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.habiti.habits.impl.common.Converters
import com.habiti.habits.impl.common.notifications.ReminderWorker
import com.habiti.habits.impl.domain.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HabitRepository(private val database: HabitsDb, private val context: Context) {
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
            lastCompletedDate = this.lastCompletedDate,
            reminderEnabled = this.reminderEnabled,
            reminderHour = this.reminderHour,
            reminderMinute = this.reminderMinute,
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
            lastCompletedDate = lastCompletedDate,
            reminderEnabled = reminderEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
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
        val id = dao.insertHabit(habit.toEntity())
        if (habit.reminderEnabled) {
            scheduleReminder(habit.copy(id = id.toString()))
        }
        return id;
    }

    // Обновить привычку
    suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(habit.toEntity())
        cancelReminder(habit.id)
        if (habit.reminderEnabled) {
            scheduleReminder(habit)
        }
    }

    // Удалить привычку
    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit.toEntity())
        cancelReminder(habit.id)
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

    private val workManager = WorkManager.getInstance(context)
    private fun scheduleReminder(habit: Habit) {
        val hour = habit.reminderHour ?: return
        val minute = habit.reminderMinute ?: return

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS  // повторяется каждый день
        ).setInitialDelay(
            calculateInitialDelay(hour, minute),
            TimeUnit.MILLISECONDS
        ).setInputData(
            workDataOf(
                "habit_id" to habit.id,
                "habit_name" to habit.name
            )
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "habit_reminder_${habit.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()

        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis - now.timeInMillis
    }

    private fun cancelReminder(habitId: String) {
        workManager.cancelUniqueWork("habit_reminder_$habitId")
    }


    // Восстановить все напоминания при запуске (вызвать в Application)
    suspend fun rescheduleAllReminders() {
        dao.getAllHabits().collect { habits ->
            habits.forEach { habit ->
                if (habit.reminderEnabled) {
                    scheduleReminder(habit.toDomain())
                }
            }
        }
    }

    suspend fun getHabitById(habitId: String): Habit? {
        val id = habitId.toLongOrNull() ?: return null
        val entity = dao.getHabitById(id)
        return entity?.toDomain()
    }
}