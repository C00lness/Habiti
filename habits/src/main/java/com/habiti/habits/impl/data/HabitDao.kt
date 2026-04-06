package com.habiti.habits.impl.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAtMillis DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("""
        UPDATE habits 
        SET 
            currentCount = currentCount + 1,
            streak = CASE 
                WHEN date(lastCompletedDate / 1000, 'unixepoch') != date('now') 
                THEN 1 
                ELSE streak + 1 
            END,
            maxStreak = MAX(maxStreak, streak + 1),
            lastCompletedDate = :timestamp
        WHERE id = :id
    """)
    suspend fun incrementProgress(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM habits WHERE name LIKE '%' || :query || '%'")
    fun searchHabits(query: String): Flow<List<HabitEntity>>
    @Query("DELETE FROM habits")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM habits")
    fun getCountSync(): Int

    @Insert
    fun insertHabitSync(habit: HabitEntity)

    @Query("UPDATE habits SET lastCompletedDate = :date WHERE id = :id")
    suspend fun updateLastCompletedDate(id: Long, date: Long)

    @Query("UPDATE habits SET lastCompletedDate = :date WHERE id = :id")
    fun updateLastCompletedDateSync(id: Long, date: Long)

}