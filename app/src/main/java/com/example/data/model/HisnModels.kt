package com.example.data.model

data class HisnCategory(
    val id: Int,
    val title: String,
    val icon: String,
    val items: List<DhikrItem>
)

data class DhikrItem(
    val id: Int,
    val text: String,
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val virtue: String = "",
    val reference: String = ""
)

data class TasbeehOption(
    val id: Int,
    val text: String,
    val virtue: String,
    val defaultTarget: Int = 33
)
