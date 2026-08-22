package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KhatmaDao {
    @Query("SELECT * FROM khatma_tracker WHERE id = 1 LIMIT 1")
    fun getKhatma(): Flow<KhatmaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveKhatma(khatma: KhatmaEntity)

    @Query("UPDATE khatma_tracker SET currentPage = :page, lastReadDateMillis = :timestamp WHERE id = 1")
    suspend fun updatePage(page: Int, timestamp: Long)

    @Query("DELETE FROM khatma_tracker")
    suspend fun resetKhatma()
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikr_progress")
    fun getAllProgress(): Flow<List<DhikrProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(entity: DhikrProgressEntity)

    @Query("DELETE FROM dhikr_progress WHERE dhikrKey = :key")
    suspend fun resetDhikr(key: String)
}
