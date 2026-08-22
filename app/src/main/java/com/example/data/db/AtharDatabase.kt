package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [KhatmaEntity::class, DhikrProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AtharDatabase : RoomDatabase() {
    abstract fun khatmaDao(): KhatmaDao
    abstract fun dhikrDao(): DhikrDao

    companion object {
        @Volatile
        private var INSTANCE: AtharDatabase? = null

        fun getInstance(context: Context): AtharDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AtharDatabase::class.java,
                    "athar_al_muslim.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
