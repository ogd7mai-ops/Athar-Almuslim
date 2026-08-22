package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "khatma_tracker")
data class KhatmaEntity(
    @PrimaryKey val id: Int = 1,
    val title: String,
    val currentPage: Int,
    val targetDailyPages: Int,
    val totalPages: Int = 604,
    val startDateMillis: Long,
    val lastReadDateMillis: Long,
    val isCompleted: Boolean = false
)
