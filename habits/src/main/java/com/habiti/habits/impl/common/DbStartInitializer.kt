package com.habiti.habits.impl.common

import com.habiti.habits.impl.data.HabitDao
import com.habiti.habits.impl.data.HabitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    /**
     * Заполняет базу данных тестовыми привычками
     */
    suspend fun populateDatabase(dao: HabitDao) {
        withContext(Dispatchers.IO) {
            // Проверяем, есть ли уже данные
            val count = dao.getCount()
            if (count > 0) {
                println("📊 База уже содержит $count записей, пропускаем инициализацию")
                return@withContext
            }

            println("🔥 Начинаем заполнение базы тестовыми данными...")

            val now = System.currentTimeMillis()

            // Создаем тестовые привычки
            val habits = listOf(
                HabitEntity(
                    name = "Зарядка",
                    description = "Утренняя зарядка 15 минут",
                    icon = "💪",
                    color = 0xFF4CAF50,
                    targetCount = 30,
                    currentCount = 5,
                    streak = 3,
                    maxStreak = 3,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = "08:00",
                    reminderDays = "1,2,3,4,5"
                ),
                HabitEntity(
                    name = "Чтение",
                    description = "Читать 30 минут в день",
                    icon = "📚",
                    color = 0xFF2196F3,
                    targetCount = 20,
                    currentCount = 12,
                    streak = 7,
                    maxStreak = 7,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = "21:00",
                    reminderDays = "1,2,3,4,5,6,7"
                ),
                HabitEntity(
                    name = "Вода",
                    description = "Пить 8 стаканов воды",
                    icon = "💧",
                    color = 0xFF00BCD4,
                    targetCount = 30,
                    currentCount = 18,
                    streak = 14,
                    maxStreak = 14,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = null,
                    reminderDays = null
                ),
                HabitEntity(
                    name = "Медитация",
                    description = "10 минут осознанности",
                    icon = "🧘",
                    color = 0xFF9C27B0,
                    targetCount = 15,
                    currentCount = 3,
                    streak = 1,
                    maxStreak = 1,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = "07:00",
                    reminderDays = "1,2,3,4,5"
                ),
                HabitEntity(
                    name = "Прогулка",
                    description = "30 минут на свежем воздухе",
                    icon = "🚶",
                    color = 0xFFFF9800,
                    targetCount = 25,
                    currentCount = 8,
                    streak = 4,
                    maxStreak = 4,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = "18:00",
                    reminderDays = "6,7"
                ),
                HabitEntity(
                    name = "Английский",
                    description = "Учить 20 новых слов",
                    icon = "🇬🇧",
                    color = 0xFFE91E63,
                    targetCount = 30,
                    currentCount = 2,
                    streak = 2,
                    maxStreak = 2,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = "12:00",
                    reminderDays = "1,3,5"
                ),
                HabitEntity(
                    name = "Спортзал",
                    description = "Тренировка",
                    icon = "🏋️",
                    color = 0xFFF44336,
                    targetCount = 12,
                    currentCount = 0,
                    streak = 0,
                    maxStreak = 0,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isArchived = false,
                    reminderTime = "19:00",
                    reminderDays = "2,4,6"
                )
            )

            // Вставляем все привычки
            habits.forEach { habit ->
                dao.insertHabit(habit)
            }

            println("✅ База успешно заполнена ${habits.size} тестовыми привычками!")
        }
    }

    /**
     * Очищает базу данных (для тестов)
     */
    suspend fun clearDatabase(dao: HabitDao) {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
            println("🗑️ База данных очищена")
        }
    }
}