package com.habiti.habits.impl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HabitEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HabitsDb : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitsDb? = null

        fun getInstance(context: Context): HabitsDb {
            INSTANCE?.let { return it }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                HabitsDb::class.java,
                                "habits_database"
                            ).fallbackToDestructiveMigration(false).build()

                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    fillDatabaseIfEmpty(instance.habitDao())
                }

                return instance
            }
        }

        private suspend fun fillDatabaseIfEmpty(dao: HabitDao) {
            val count = dao.getCountSync()

            if (count == 0) {

                val now = System.currentTimeMillis()

                val habits = listOf(
                    HabitEntity(
                        name = "Зарядка",
                        icon = "💪",
                        color = 0xFF4CAF50,
                        targetCount = 30,
                        currentCount = 5,
                        streak = 3,
                        maxStreak = 3,
                        createdAtMillis = now,
                        updatedAtMillis = now,
                        isArchived = false,
                        reminderTime = null,
                        reminderDays = null,
                        description = ""
                    ),
                    HabitEntity(
                        name = "Чтение",
                        icon = "📚",
                        color = 0xFF2196F3,
                        targetCount = 20,
                        currentCount = 12,
                        streak = 7,
                        maxStreak = 7,
                        createdAtMillis = now,
                        updatedAtMillis = now,
                        isArchived = false,
                        reminderTime = null,
                        reminderDays = null,
                        description = ""
                    ),
                    HabitEntity(
                        name = "Вода",
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
                        reminderDays = null,
                        description = ""
                    )
                )

                habits.forEach { habit ->
                    dao.insertHabitSync(habit)
                }
            }
        }
    }
}