package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MuezzinVoice
import com.example.data.model.Reciter
import com.example.data.repository.AudioRepository
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioUiState(
    val reciters: List<Reciter> = emptyList(),
    val selectedReciter: Reciter? = null,
    val currentSurahId: Int = 1,
    val currentSurahNameAr: String = "الفاتحة",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMillis: Int = 0,
    val durationMillis: Int = 0,
    val muezzinVoices: List<MuezzinVoice> = emptyList(),
    val activeMuezzinPreviewId: String? = null
)

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val audioRepo = AudioRepository()
    private val quranRepo = QuranRepository(com.example.data.db.AtharDatabase.getInstance(application).khatmaDao())

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        val reciters = audioRepo.getReciters()
        val muezzins = audioRepo.getMuezzinVoices()
        _uiState.value = _uiState.value.copy(
            reciters = reciters,
            selectedReciter = reciters.firstOrNull(),
            muezzinVoices = muezzins
        )
        startPositionTracker()
    }

    fun selectReciter(reciter: Reciter) {
        val wasPlaying = _uiState.value.isPlaying
        _uiState.value = _uiState.value.copy(selectedReciter = reciter)
        if (wasPlaying) {
            playSurah(_uiState.value.currentSurahId)
        }
    }

    fun playSurah(surahId: Int) {
        val reciter = _uiState.value.selectedReciter ?: return
        val surah = quranRepo.getSurahById(surahId) ?: return

        stopAudio()

        val audioUrl = audioRepo.getAudioUrlForSurah(reciter, surahId)
        _uiState.value = _uiState.value.copy(
            currentSurahId = surahId,
            currentSurahNameAr = surah.nameArabic,
            isBuffering = true,
            isPlaying = false
        )

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMillis = mp.duration
                    )
                    mp.start()
                }
                setOnCompletionListener {
                    _uiState.value = _uiState.value.copy(
                        isPlaying = false,
                        currentPositionMillis = 0
                    )
                    // Auto play next surah if available
                    if (surahId < 114) {
                        playSurah(surahId + 1)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        isPlaying = false
                    )
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                isBuffering = false,
                isPlaying = false
            )
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        if (mp != null) {
            if (mp.isPlaying) {
                mp.pause()
                _uiState.value = _uiState.value.copy(isPlaying = false)
            } else {
                mp.start()
                _uiState.value = _uiState.value.copy(isPlaying = true)
            }
        } else {
            playSurah(_uiState.value.currentSurahId)
        }
    }

    fun seekTo(positionMillis: Int) {
        mediaPlayer?.seekTo(positionMillis)
        _uiState.value = _uiState.value.copy(currentPositionMillis = positionMillis)
    }

    fun previewMuezzinVoice(voice: MuezzinVoice) {
        stopAudio()
        _uiState.value = _uiState.value.copy(
            activeMuezzinPreviewId = voice.id,
            isBuffering = true
        )

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(voice.sampleAudioUrl)
                setOnPreparedListener { mp ->
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMillis = mp.duration
                    )
                    mp.start()
                }
                setOnCompletionListener {
                    _uiState.value = _uiState.value.copy(
                        isPlaying = false,
                        activeMuezzinPreviewId = null
                    )
                }
                setOnErrorListener { _, _, _ ->
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        activeMuezzinPreviewId = null
                    )
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                isBuffering = false,
                isPlaying = false,
                activeMuezzinPreviewId = null
            )
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            isBuffering = false,
            activeMuezzinPreviewId = null
        )
    }

    private fun startPositionTracker() {
        viewModelScope.launch {
            while (true) {
                delay(500L)
                val mp = mediaPlayer
                if (mp != null && _uiState.value.isPlaying) {
                    try {
                        _uiState.value = _uiState.value.copy(
                            currentPositionMillis = mp.currentPosition,
                            durationMillis = mp.duration
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
