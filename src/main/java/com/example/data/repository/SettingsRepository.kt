package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val selectedMuezzinId: String = "makkah",
    val selectedReciterId: String = "afs",
    val useGpsLocation: Boolean = true,
    val selectedCityName: String = "مكة المكرمة",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val calculationMethodName: String = "UMM_AL_QURA",
    val enableFajrNotification: Boolean = true,
    val enableDhuhrNotification: Boolean = true,
    val enableAsrNotification: Boolean = true,
    val enableMaghribNotification: Boolean = true,
    val enableIshaNotification: Boolean = true,
    val enableVibration: Boolean = true
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("athar_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            selectedMuezzinId = prefs.getString("muezzin_id", "makkah") ?: "makkah",
            selectedReciterId = prefs.getString("reciter_id", "afs") ?: "afs",
            useGpsLocation = prefs.getBoolean("use_gps", true),
            selectedCityName = prefs.getString("city_name", "مكة المكرمة") ?: "مكة المكرمة",
            latitude = prefs.getFloat("lat", 21.4225f).toDouble(),
            longitude = prefs.getFloat("lon", 39.8262f).toDouble(),
            calculationMethodName = prefs.getString("calc_method", "UMM_AL_QURA") ?: "UMM_AL_QURA",
            enableFajrNotification = prefs.getBoolean("notif_fajr", true),
            enableDhuhrNotification = prefs.getBoolean("notif_dhuhr", true),
            enableAsrNotification = prefs.getBoolean("notif_asr", true),
            enableMaghribNotification = prefs.getBoolean("notif_maghrib", true),
            enableIshaNotification = prefs.getBoolean("notif_isha", true),
            enableVibration = prefs.getBoolean("notif_vibrate", true)
        )
    }

    fun updateMuezzin(muezzinId: String) {
        prefs.edit().putString("muezzin_id", muezzinId).apply()
        _settings.value = _settings.value.copy(selectedMuezzinId = muezzinId)
    }

    fun updateReciter(reciterId: String) {
        prefs.edit().putString("reciter_id", reciterId).apply()
        _settings.value = _settings.value.copy(selectedReciterId = reciterId)
    }

    fun updateLocationMode(useGps: Boolean) {
        prefs.edit().putBoolean("use_gps", useGps).apply()
        _settings.value = _settings.value.copy(useGpsLocation = useGps)
    }

    fun updateCity(cityName: String, lat: Double, lon: Double) {
        prefs.edit()
            .putString("city_name", cityName)
            .putFloat("lat", lat.toFloat())
            .putFloat("lon", lon.toFloat())
            .apply()
        _settings.value = _settings.value.copy(
            selectedCityName = cityName,
            latitude = lat,
            longitude = lon
        )
    }

    fun updateNotificationToggle(prayerKey: String, enabled: Boolean) {
        val prefKey = "notif_$prayerKey"
        prefs.edit().putBoolean(prefKey, enabled).apply()
        _settings.value = when (prayerKey) {
            "fajr" -> _settings.value.copy(enableFajrNotification = enabled)
            "dhuhr" -> _settings.value.copy(enableDhuhrNotification = enabled)
            "asr" -> _settings.value.copy(enableAsrNotification = enabled)
            "maghrib" -> _settings.value.copy(enableMaghribNotification = enabled)
            "isha" -> _settings.value.copy(enableIshaNotification = enabled)
            else -> _settings.value
        }
    }
}
