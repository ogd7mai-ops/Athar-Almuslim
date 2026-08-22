package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AtharDatabase
import com.example.data.model.DhikrItem
import com.example.data.model.HisnCategory
import com.example.data.model.TasbeehOption
import com.example.data.repository.HisnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HisnUiState(
    val selectedTab: Int = 0, // 0: Morning/Evening, 1: Hisn Al-Muslim, 2: Tasbeeh
    val selectedDhikrType: String = "MORNING", // MORNING, EVENING, AFTER_PRAYER
    val activeDhikrList: List<DhikrItem> = emptyList(),
    val dhikrCounts: Map<String, Int> = emptyMap(),
    val hisnCategories: List<HisnCategory> = emptyList(),
    val selectedHisnCategory: HisnCategory? = null,
    val tasbeehOptions: List<TasbeehOption> = emptyList(),
    val selectedTasbeeh: TasbeehOption? = null,
    val tasbeehCount: Int = 0,
    val totalTasbeehSession: Int = 0
)

class HisnViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AtharDatabase.getInstance(application)
    private val repo = HisnRepository(db.dhikrDao())

    private val _uiState = MutableStateFlow(HisnUiState())
    val uiState: StateFlow<HisnUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("athar_tasbeeh", Context.MODE_PRIVATE)

    init {
        loadData()
        observeSavedProgress()
    }

    private fun loadData() {
        val morning = repo.getMorningAdhkar()
        val categories = repo.getHisnCategories()
        val options = repo.getTasbeehOptions()
        val savedTotal = prefs.getInt("total_tasbeeh_count", 0)

        _uiState.value = _uiState.value.copy(
            activeDhikrList = morning,
            hisnCategories = categories,
            tasbeehOptions = options,
            selectedTasbeeh = options.firstOrNull(),
            totalTasbeehSession = savedTotal
        )
    }

    private fun observeSavedProgress() {
        viewModelScope.launch {
            repo.savedProgress.collect { list ->
                val map = list.associate { it.dhikrKey to it.count }
                _uiState.value = _uiState.value.copy(dhikrCounts = map)
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun selectDhikrType(type: String) {
        val list = when (type) {
            "MORNING" -> repo.getMorningAdhkar()
            "EVENING" -> repo.getEveningAdhkar()
            "AFTER_PRAYER" -> repo.getPostPrayerAdhkar()
            else -> repo.getMorningAdhkar()
        }
        _uiState.value = _uiState.value.copy(
            selectedDhikrType = type,
            activeDhikrList = list
        )
    }

    fun selectHisnCategory(category: HisnCategory?) {
        _uiState.value = _uiState.value.copy(selectedHisnCategory = category)
    }

    fun selectTasbeehOption(option: TasbeehOption) {
        _uiState.value = _uiState.value.copy(
            selectedTasbeeh = option,
            tasbeehCount = 0
        )
    }

    fun incrementDhikrCount(dhikrKey: String, maxCount: Int) {
        triggerHapticFeedback()
        val current = _uiState.value.dhikrCounts[dhikrKey] ?: 0
        if (current < maxCount) {
            val next = current + 1
            viewModelScope.launch {
                repo.updateDhikrCount(dhikrKey, next)
            }
        }
    }

    fun resetDhikrCount(dhikrKey: String) {
        viewModelScope.launch {
            repo.resetDhikr(dhikrKey)
        }
    }

    fun incrementTasbeeh() {
        triggerHapticFeedback()
        val newCount = _uiState.value.tasbeehCount + 1
        val newTotal = _uiState.value.totalTasbeehSession + 1
        prefs.edit().putInt("total_tasbeeh_count", newTotal).apply()
        _uiState.value = _uiState.value.copy(
            tasbeehCount = newCount,
            totalTasbeehSession = newTotal
        )
    }

    fun resetTasbeeh() {
        _uiState.value = _uiState.value.copy(tasbeehCount = 0)
    }

    private fun triggerHapticFeedback() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
