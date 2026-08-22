package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AtharDatabase
import com.example.data.model.HijriCalendarHelper
import com.example.data.model.HijriDate
import com.example.data.model.KhatmaState
import com.example.data.model.PrayerSchedule
import com.example.data.repository.NextPrayerInfo
import com.example.data.repository.PrayerRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.receiver.PrayerAlarmReceiver
import com.example.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val prayerSchedule: PrayerSchedule? = null,
    val nextPrayerInfo: NextPrayerInfo? = null,
    val hijriDate: HijriDate = HijriCalendarHelper.getTodayHijri(),
    val locationName: String = "مكة المكرمة، المملكة العربية السعودية",
    val currentTimeFormatted: String = "",
    val khatmaState: KhatmaState = KhatmaState(),
    val lastReadSurahName: String = "",
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prayerRepo = PrayerRepository()
    private val settingsRepo = SettingsRepository(application)
    private val quranRepo = QuranRepository(AtharDatabase.getInstance(application).khatmaDao())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        observeKhatma()
        checkDeviceLocation()
        startTimer()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepo.settings.collect { settings ->
                var resolvedLocationName = settings.selectedCityName
                if (settings.useGpsLocation) {
                    val deviceLoc = LocationHelper.getCurrentDeviceLocation(getApplication())
                    if (deviceLoc != null) {
                        resolvedLocationName = LocationHelper.getCityAndCountryName(
                            getApplication(),
                            deviceLoc.latitude,
                            deviceLoc.longitude
                        )
                        settingsRepo.updateCity(resolvedLocationName, deviceLoc.latitude, deviceLoc.longitude)
                    } else {
                        resolvedLocationName = LocationHelper.getCityAndCountryName(
                            getApplication(),
                            settings.latitude,
                            settings.longitude
                        )
                    }
                } else {
                    resolvedLocationName = LocationHelper.getCityAndCountryName(
                        getApplication(),
                        settings.latitude,
                        settings.longitude
                    )
                }

                refreshSchedule(
                    lat = settings.latitude,
                    lon = settings.longitude,
                    locationName = resolvedLocationName,
                    calcMethodName = settings.calculationMethodName
                )
            }
        }
    }

    private fun observeKhatma() {
        viewModelScope.launch {
            quranRepo.khatmaState.collect { state ->
                var surahName = ""
                if (state.currentPage > 0) {
                    val surah = quranRepo.getAllSurahs().find { state.currentPage >= it.startPage }
                        ?: quranRepo.getAllSurahs().firstOrNull()
                    surahName = surah?.nameArabic ?: ""
                }
                _uiState.value = _uiState.value.copy(
                    khatmaState = state,
                    lastReadSurahName = surahName
                )
            }
        }
    }

    private fun checkDeviceLocation() {
        viewModelScope.launch {
            try {
                val settings = settingsRepo.settings.value
                if (settings.useGpsLocation) {
                    val deviceLoc = LocationHelper.getCurrentDeviceLocation(getApplication())
                    if (deviceLoc != null) {
                        val cityCountry = LocationHelper.getCityAndCountryName(
                            getApplication(),
                            deviceLoc.latitude,
                            deviceLoc.longitude
                        )
                        updateGpsCoordinates(deviceLoc.latitude, deviceLoc.longitude, cityCountry)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshSchedule(
        lat: Double,
        lon: Double,
        locationName: String,
        calcMethodName: String = "UMM_AL_QURA"
    ) {
        val schedule = prayerRepo.getPrayerSchedule(
            latitude = lat,
            longitude = lon,
            locationName = locationName,
            methodName = calcMethodName
        )
        val nextInfo = prayerRepo.getNextPrayerInfo(
            schedule = schedule,
            lat = lat,
            lon = lon,
            methodName = calcMethodName
        )
        _uiState.value = _uiState.value.copy(
            prayerSchedule = schedule,
            nextPrayerInfo = nextInfo,
            currentTimeFormatted = prayerRepo.getCurrentClockFormatted(),
            hijriDate = HijriCalendarHelper.getTodayHijri(),
            locationName = locationName
        )
        PrayerAlarmReceiver.scheduleNextPrayerAlarm(getApplication())
    }

    fun updateGpsCoordinates(lat: Double, lon: Double, detectedCity: String) {
        settingsRepo.updateCity(detectedCity, lat, lon)
        refreshSchedule(lat, lon, detectedCity)
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val currentSchedule = _uiState.value.prayerSchedule
                val currentSettings = settingsRepo.settings.value
                if (currentSchedule != null) {
                    val nextInfo = prayerRepo.getNextPrayerInfo(
                        schedule = currentSchedule,
                        lat = currentSettings.latitude,
                        lon = currentSettings.longitude,
                        methodName = currentSettings.calculationMethodName
                    )
                    _uiState.value = _uiState.value.copy(
                        nextPrayerInfo = nextInfo,
                        currentTimeFormatted = prayerRepo.getCurrentClockFormatted(),
                        hijriDate = HijriCalendarHelper.getTodayHijri()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentTimeFormatted = prayerRepo.getCurrentClockFormatted()
                    )
                }
                delay(1000L)
            }
        }
    }
}
