package com.example.data.model

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: RevelationType,
    val startPage: Int,
    val ayahs: List<Ayah> = emptyList()
)

enum class RevelationType(val arName: String) {
    MAKKI("مكية"),
    MADANI("مدنية")
}

data class Ayah(
    val numberInSurah: Int,
    val textUthmani: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val isBismillah: Boolean = false
)

data class KhatmaState(
    val id: Int = 1,
    val title: String = "ختمة القرآن الكريم",
    val startPage: Int = 1,
    val currentPage: Int = 1,
    val targetDailyPages: Int = 4,
    val totalPages: Int = 604,
    val startDateMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    val progressPercentage: Float
        get() = (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) * 100f

    val remainingPages: Int
        get() = (totalPages - currentPage).coerceAtLeast(0)

    val estimatedDaysLeft: Int
        get() = if (targetDailyPages > 0) (remainingPages + targetDailyPages - 1) / targetDailyPages else 0
}
