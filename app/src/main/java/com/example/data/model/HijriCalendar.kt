package com.example.data.model

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Locale

data class HijriDate(
    val day: Int,
    val monthName: String,
    val monthNumber: Int,
    val year: Int,
    val dayOfWeekName: String,
    val gregorianFormatted: String
)

object HijriCalendarHelper {

    private val hijriMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val daysOfWeekArabic = mapOf(
        "SUNDAY" to "الأحد",
        "MONDAY" to "الإثنين",
        "TUESDAY" to "الثلاثاء",
        "WEDNESDAY" to "الأربعاء",
        "THURSDAY" to "الخميس",
        "FRIDAY" to "الجمعة",
        "SATURDAY" to "السبت"
    )

    fun getTodayHijri(): HijriDate {
        return try {
            val hijrahDate = HijrahDate.now()
            val hijriYear = hijrahDate.get(ChronoField.YEAR)
            val hijriMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val hijriDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)

            val monthName = hijriMonths.getOrElse(hijriMonth - 1) { "محرم" }

            val todayGregorian = LocalDate.now()
            val dayOfWeekKey = todayGregorian.dayOfWeek.name
            val dayName = daysOfWeekArabic[dayOfWeekKey] ?: "الأحد"

            val gregDay = todayGregorian.dayOfMonth
            val gregMonth = todayGregorian.monthValue
            val gregYear = todayGregorian.year

            val gregFormatted = String.format(
                Locale("ar"),
                "%s %d %s %d مـ",
                dayName,
                gregDay,
                getGregorianMonthName(gregMonth),
                gregYear
            )

            HijriDate(
                day = hijriDay,
                monthName = monthName,
                monthNumber = hijriMonth,
                year = hijriYear,
                dayOfWeekName = dayName,
                gregorianFormatted = gregFormatted
            )
        } catch (e: Exception) {
            HijriDate(
                day = 1,
                monthName = "محرم",
                monthNumber = 1,
                year = 1448,
                dayOfWeekName = "الجمعة",
                gregorianFormatted = ""
            )
        }
    }

    private fun getGregorianMonthName(month: Int): String {
        return when (month) {
            1 -> "يناير"
            2 -> "فبراير"
            3 -> "مارس"
            4 -> "أبريل"
            5 -> "مايو"
            6 -> "يونيو"
            7 -> "يوليو"
            8 -> "أغسطس"
            9 -> "سبتمبر"
            10 -> "أكتوبر"
            11 -> "نوفمبر"
            12 -> "ديسمبر"
            else -> ""
        }
    }
}

