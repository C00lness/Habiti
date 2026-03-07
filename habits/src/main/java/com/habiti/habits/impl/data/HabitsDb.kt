package com.habiti.habits.impl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habiti.habits.impl.common.Converters

@Database(
    entities = [HabitEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitsDb: RoomDatabase() {
    abstract fun habitDao(): HabitDao
    companion object {
        @Volatile
        private var INSTANCE: HabitsDb? = null

        fun getInstance(context: Context): HabitsDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitsDb::class.java,
                    "habiti_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}