package com.habiti.habits.impl.common

object Converters {
    fun fromDaysList(days: List<Int>?): String? {
        return days?.joinToString(",")
    }

    fun toDaysList(daysString: String?): List<Int>? {
        return daysString?.split(",")?.mapNotNull { it.toIntOrNull() }
    }
}