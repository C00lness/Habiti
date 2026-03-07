package com.habiti.habits.impl.domain

import android.util.Log
import java.util.Calendar

data class Habit(
    val id: String,
    val name: String,
    val description: String?,
    val icon: String,
    val color: Long,
    val targetCount: Int,
    val currentCount: Int,
    val streak: Int,
    val maxStreak: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isArchived: Boolean,
    val reminderTime: String?,
    val reminderDays: List<Int>?,
    val lastCompletedDate: Long? = null
) {
    // Вычисляемые поля (для удобства в UI)
    val progress: Float
        get() = if (targetCount > 0) currentCount.toFloat() / targetCount else 0f

    val remainingCount: Int
        get() = (targetCount - currentCount).coerceAtLeast(0)

    val isCompleted: Boolean
        get() = currentCount >= targetCount

    val isCompletedToday: Boolean
        get() {
            if (lastCompletedDate == null) return false

            val todayCal = Calendar.getInstance()
            val lastCal = Calendar.getInstance().apply {
                timeInMillis = lastCompletedDate
            }

            return todayCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR) &&
                    todayCal.get(Calendar.DAY_OF_YEAR) == lastCal.get(Calendar.DAY_OF_YEAR)
        }
}