package com.habiti.habits.impl.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("habit_history")

class HabitHistoryRepository(private val context: Context) {

    // Сохранить историю для конкретной привычки
    suspend fun saveHistory(habitId: String, history: List<Pair<Long, Boolean>>) {
        // Кодируем список в строку: "timestamp,1|timestamp,0|..."
        val json = history.joinToString("|") { "${it.first},${it.second}" }
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(habitId)] = json
        }
    }

    // Получить историю для привычки (Flow)
    fun getHistory(habitId: String): Flow<List<Pair<Long, Boolean>>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[stringPreferencesKey(habitId)] ?: return@map emptyList()
            if (json.isBlank()) return@map emptyList()
            json.split("|").mapNotNull { part ->
                val parts = part.split(",")
                if (parts.size == 2) {
                    val timestamp = parts[0].toLongOrNull()
                    val done = parts[1].toBooleanStrictOrNull()
                    if (timestamp != null && done != null) {
                        timestamp to done
                    } else null
                } else null
            }
        }
    }

    // Добавить одну запись в историю
    suspend fun addRecord(habitId: String, done: Boolean) {
        val timestamp = System.currentTimeMillis()
        // Получаем текущую историю, добавляем новую запись и сохраняем
        val currentHistory = getHistory(habitId).firstOrNull() ?: emptyList()
        val newHistory = currentHistory + (timestamp to done)
        saveHistory(habitId, newHistory)
    }

    // Получить историю за последние N дней
    suspend fun getLastNDays(habitId: String, days: Int): List<Pair<Long, Boolean>> {
        val history = getHistory(habitId).firstOrNull() ?: return emptyList()
        val cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
        return history.filter { it.first >= cutoff }
    }
}