package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_progress")
data class DhikrProgressEntity(
    @PrimaryKey val dhikrKey: String,
    val count: Int,
    val lastUpdatedMillis: Long
)
