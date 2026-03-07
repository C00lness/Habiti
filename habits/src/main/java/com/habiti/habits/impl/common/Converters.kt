package com.habiti.habits.impl.common

import androidx.room.TypeConverter

object Converters {
    @TypeConverter
    fun fromDaysList(days: List<Int>?): String? {
        return days?.joinToString(",")
    }

    @TypeConverter
    fun toDaysList(daysString: String?): List<Int>? {
        return daysString?.split(",")?.mapNotNull { it.toIntOrNull() }
    }
}