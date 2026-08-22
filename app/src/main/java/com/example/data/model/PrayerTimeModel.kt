package com.example.data.model

import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class PrayerType(val arName: String, val icon: String) {
    FAJR("الفجر", "🌅"),
    SUNRISE("الشروق", "☀️"),
    DHUHR("الظهر", "☀️"),
    ASR("العصر", "🌤️"),
    MAGHRIB("المغرب", "🌅"),
    ISHA("العشاء", "🌙")
}

data class PrayerTime(
    val type: PrayerType,
    val timeMillis: Long,
    val formattedTime: String
)

data class PrayerSchedule(
    val fajr: Long,
    val sunrise: Long,
    val dhuhr: Long,
    val asr: Long,
    val maghrib: Long,
    val isha: Long,
    val dateString: String,
    val locationName: String
)

object PrayerCalculator {

    // Calculation Method Angle/Intervals
    enum class CalculationMethod(
        val arName: String,
        val fajrAngle: Double,
        val ishaAngle: Double,
        val ishaIntervalMinutes: Int = 0
    ) {
        UMM_AL_QURA("أم القرى (مكة المكرمة)", 18.5, 0.0, 90),
        EGYPTIAN("الهيئة المصرية العامة للمساحة", 19.5, 17.5),
        MWL("رابطة العالم الإسلامي", 18.0, 17.0),
        ISNA("أمريكا الشمالية (ISNA)", 15.0, 15.0),
        KARACHI("جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0)
    }

    fun calculateSchedule(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        locationName: String = "الموقع الحالي"
    ): PrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val timezone = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        val d = julianDay(year, month, day) - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.0000004 * d
        val ra = Math.toDegrees(
            kotlin.math.atan2(
                cos(Math.toRadians(e)) * sin(Math.toRadians(l)),
                cos(Math.toRadians(l))
            )
        ) / 15.0
        val raFixed = fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))

        var eqTime = q / 15.0 - raFixed
        if (eqTime > 20) eqTime -= 24
        if (eqTime < -20) eqTime += 24

        // Dhuhr
        val dhuhrHours = fixHour(12.0 + timezone - (longitude / 15.0) - eqTime)

        // Helper function for Hour Angle H given altitude angle (in degrees, + above horizon, - below)
        fun hourAngle(altitudeDeg: Double): Double {
            val sinAlt = sin(Math.toRadians(altitudeDeg))
            val sinLat = sin(Math.toRadians(latitude))
            val sinDecl = sin(Math.toRadians(decl))
            val cosLat = cos(Math.toRadians(latitude))
            val cosDecl = cos(Math.toRadians(decl))

            val cosH = (sinAlt - sinLat * sinDecl) / (cosLat * cosDecl)
            return when {
                cosH >= 1.0 -> 0.0
                cosH <= -1.0 -> 180.0
                else -> Math.toDegrees(acos(cosH))
            }
        }

        // Sunrise & Sunset (center of sun is -0.833 degrees below horizon)
        val sunriseH = hourAngle(-0.833)
        val sunriseHours = dhuhrHours - sunriseH / 15.0
        val sunsetHours = dhuhrHours + sunriseH / 15.0

        // Fajr
        val fajrH = hourAngle(-method.fajrAngle)
        val fajrHours = dhuhrHours - fajrH / 15.0

        // Asr (Shafi / General: shadow factor = 1)
        val asrElevation = Math.toDegrees(atan(1.0 / (1.0 + tan(Math.toRadians(abs(latitude - decl))))))
        val asrH = hourAngle(asrElevation)
        val asrHours = dhuhrHours + asrH / 15.0

        // Maghrib
        val maghribHours = sunsetHours

        // Isha
        val ishaHours = if (method.ishaIntervalMinutes > 0) {
            maghribHours + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaH = hourAngle(-method.ishaAngle)
            dhuhrHours + ishaH / 15.0
        }

        fun toMillis(hours: Double): Long {
            val baseCal = (calendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val totalSec = (hours * 3600).toLong()
            return baseCal.timeInMillis + totalSec * 1000L
        }

        val dateFormatted = String.format(
            Locale("ar"),
            "%02d/%02d/%04d",
            day, month, year
        )

        return PrayerSchedule(
            fajr = toMillis(fajrHours),
            sunrise = toMillis(sunriseHours),
            dhuhr = toMillis(dhuhrHours),
            asr = toMillis(asrHours),
            maghrib = toMillis(maghribHours),
            isha = toMillis(ishaHours),
            dateString = dateFormatted,
            locationName = locationName
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }
}
