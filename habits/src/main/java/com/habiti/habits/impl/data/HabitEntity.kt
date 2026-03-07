package com.habiti.habits.impl.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val reminderDays: String?
)
