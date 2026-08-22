package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PrayerCalculator
import com.example.data.model.Reciter
import com.example.data.model.MuezzinVoice
import com.example.data.repository.AudioRepository
import com.example.data.repository.CityLocation
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val prayerRepo = PrayerRepository()
    private val audioRepo = AudioRepository()

    val userSettings: StateFlow<UserSettings> = settingsRepo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    val availableCities: List<CityLocation> = prayerRepo.popularCities
    val availableReciters: List<Reciter> = audioRepo.getReciters()
    val availableMuezzins: List<MuezzinVoice> = audioRepo.getMuezzinVoices()
    val calculationMethods: Array<PrayerCalculator.CalculationMethod> = PrayerCalculator.CalculationMethod.values()

    fun updateMuezzin(muezzinId: String) {
        settingsRepo.updateMuezzin(muezzinId)
    }

    fun updateReciter(reciterId: String) {
        settingsRepo.updateReciter(reciterId)
    }

    fun toggleLocationMode(useGps: Boolean) {
        settingsRepo.updateLocationMode(useGps)
    }

    fun selectCity(city: CityLocation) {
        settingsRepo.updateCity(city.cityNameAr, city.latitude, city.longitude)
    }

    fun togglePrayerNotification(prayerKey: String, enabled: Boolean) {
        settingsRepo.updateNotificationToggle(prayerKey, enabled)
    }
}
