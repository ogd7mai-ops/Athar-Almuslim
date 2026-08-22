package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AtharDatabase
import com.example.data.model.Ayah
import com.example.data.model.KhatmaState
import com.example.data.model.Surah
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuranUiState(
    val searchQuery: String = "",
    val filteredSurahs: List<Surah> = emptyList(),
    val selectedSurah: Surah? = null,
    val activeAyahs: List<Ayah> = emptyList()
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AtharDatabase.getInstance(application)
    private val repo = QuranRepository(db.khatmaDao())

    private val _uiState = MutableStateFlow(QuranUiState(filteredSurahs = repo.getAllSurahs()))
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    val khatmaState: StateFlow<KhatmaState> = repo.khatmaState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KhatmaState()
    )

    fun onSearchQueryChange(query: String) {
        val filtered = repo.searchSurahs(query)
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredSurahs = filtered
        )
    }

    fun selectSurah(surah: Surah) {
        val ayahs = repo.getAyahsForSurah(surah.id)
        _uiState.value = _uiState.value.copy(
            selectedSurah = surah,
            activeAyahs = ayahs
        )
    }

    fun clearSelectedSurah() {
        _uiState.value = _uiState.value.copy(
            selectedSurah = null,
            activeAyahs = emptyList()
        )
    }

    fun saveKhatma(currentPage: Int, targetDailyPages: Int) {
        viewModelScope.launch {
            repo.saveKhatma(currentPage, targetDailyPages)
        }
    }

    fun updateKhatmaCurrentPage(page: Int) {
        viewModelScope.launch {
            repo.updateCurrentPage(page)
        }
    }

    fun resetKhatma() {
        viewModelScope.launch {
            repo.resetKhatma()
        }
    }
}
