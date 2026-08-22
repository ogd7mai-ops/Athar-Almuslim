package com.example.data.repository

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.data.model.PrayerSchedule
import com.example.data.model.PrayerType
import java.util.Calendar
import java.util.Locale

data class CityLocation(
    val cityNameAr: String,
    val countryNameAr: String,
    val latitude: Double,
    val longitude: Double
)

data class NextPrayerInfo(
    val prayerType: PrayerType,
    val prayerTimeMillis: Long,
    val timeRemainingMillis: Long,
    val formattedCountdown: String
)

class PrayerRepository {

    val popularCities = listOf(
        CityLocation("مكة المكرمة", "المملكة العربية السعودية", 21.4225, 39.8262),
        CityLocation("المدينة المنورة", "المملكة العربية السعودية", 24.4672, 39.6112),
        CityLocation("الرياض", "المملكة العربية السعودية", 24.7136, 46.6753),
        CityLocation("القدس الشريف", "فلسطين", 31.7683, 35.2137),
        CityLocation("القاهرة", "مصر", 30.0444, 31.2357),
        CityLocation("عَمّان", "الأردن", 31.9539, 35.9106),
        CityLocation("أبوظبي", "الإمارات العربية المتحدة", 24.4539, 54.3773),
        CityLocation("الكويت", "الكويت", 29.3759, 47.9774),
        CityLocation("بغداد", "العراق", 33.3152, 44.3661),
        CityLocation("تونس", "تونس", 36.8065, 10.1815),
        CityLocation("الرباط", "المغرب", 34.0209, -6.8416),
        CityLocation("الدوحة", "قطر", 25.2854, 51.5310),
        CityLocation("مسقط", "عُمان", 23.5880, 58.3829),
        CityLocation("إسطنبول", "تركيا", 41.0082, 28.9784)
    )

    fun getPrayerSchedule(
        latitude: Double,
        longitude: Double,
        locationName: String = "الموقع الحالي",
        methodName: String = "UMM_AL_QURA"
    ): PrayerSchedule {
        val calendar = Calendar.getInstance()
        val dateComponents = DateComponents.from(calendar.time)
        val coordinates = Coordinates(latitude, longitude)

        val calcMethod = try {
            CalculationMethod.valueOf(methodName)
        } catch (e: Exception) {
            CalculationMethod.UMM_AL_QURA
        }

        val params = calcMethod.parameters
        params.madhab = Madhab.SHAFI

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val dateFormatted = String.format(Locale("ar"), "%02d/%02d/%04d", day, month, year)

        return PrayerSchedule(
            fajr = prayerTimes.fajr.time,
            sunrise = prayerTimes.sunrise.time,
            dhuhr = prayerTimes.dhuhr.time,
            asr = prayerTimes.asr.time,
            maghrib = prayerTimes.maghrib.time,
            isha = prayerTimes.isha.time,
            dateString = dateFormatted,
            locationName = locationName
        )
    }

    fun getNextPrayerInfo(
        schedule: PrayerSchedule,
        lat: Double = 21.4225,
        lon: Double = 39.8262,
        methodName: String = "UMM_AL_QURA"
    ): NextPrayerInfo {
        val now = System.currentTimeMillis()
        val prayers = listOf(
            PrayerType.FAJR to schedule.fajr,
            PrayerType.SUNRISE to schedule.sunrise,
            PrayerType.DHUHR to schedule.dhuhr,
            PrayerType.ASR to schedule.asr,
            PrayerType.MAGHRIB to schedule.maghrib,
            PrayerType.ISHA to schedule.isha
        )

        val upcoming = prayers.firstOrNull { it.second > now }

        return if (upcoming != null) {
            val remaining = upcoming.second - now
            NextPrayerInfo(
                prayerType = upcoming.first,
                prayerTimeMillis = upcoming.second,
                timeRemainingMillis = remaining,
                formattedCountdown = formatCountdown(remaining)
            )
        } else {
            val tomorrowCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowDateComp = DateComponents.from(tomorrowCal.time)
            val calcMethod = try {
                CalculationMethod.valueOf(methodName)
            } catch (e: Exception) {
                CalculationMethod.UMM_AL_QURA
            }
            val params = calcMethod.parameters
            params.madhab = Madhab.SHAFI

            val tomorrowTimes = PrayerTimes(Coordinates(lat, lon), tomorrowDateComp, params)
            val tomorrowFajr = tomorrowTimes.fajr.time
            val remaining = tomorrowFajr - now

            NextPrayerInfo(
                prayerType = PrayerType.FAJR,
                prayerTimeMillis = tomorrowFajr,
                timeRemainingMillis = remaining,
                formattedCountdown = formatCountdown(remaining)
            )
        }
    }

    fun formatCountdown(remainingMillis: Long): String {
        if (remainingMillis <= 0) return "00:00:00"
        val totalSeconds = remainingMillis / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return String.format(Locale("ar"), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatTime(timeMillis: Long): String {
        val cal = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
        val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = cal.get(Calendar.MINUTE)
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "ص" else "م"

        return String.format(Locale("ar"), "%02d:%02d %s", hour12, minute, amPm)
    }

    fun getCurrentClockFormatted(): String {
        val cal = Calendar.getInstance()
        val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "ص" else "م"

        return String.format(Locale("ar"), "%02d:%02d:%02d %s", hour12, minute, second, amPm)
    }
}

