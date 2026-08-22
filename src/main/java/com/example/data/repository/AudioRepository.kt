package com.example.data.repository

import com.example.data.model.MuezzinVoice
import com.example.data.model.Reciter

class AudioRepository {

    fun getReciters(): List<Reciter> = listOf(
        Reciter(
            id = "afs",
            name = "الشيخ مشاري بن راشد العفاسي",
            rewaya = "حفص عن عاصم",
            serverUrl = "https://server8.mp3quran.net/afs/"
        ),
        Reciter(
            id = "basit",
            name = "الشيخ عبدالباسط عبدالصمد",
            rewaya = "المصحف المجود",
            serverUrl = "https://server7.mp3quran.net/basit/"
        ),
        Reciter(
            id = "hussary",
            name = "الشيخ محمود خليل الحصري",
            rewaya = "حفص عن عاصم - المرتل",
            serverUrl = "https://server13.mp3quran.net/hussary/"
        ),
        Reciter(
            id = "ghamdi",
            name = "الشيخ سعد الغامدي",
            rewaya = "حفص عن عاصم",
            serverUrl = "https://server7.mp3quran.net/s_gmd/"
        ),
        Reciter(
            id = "shuraim",
            name = "الشيخ سعود الشريم",
            rewaya = "حفص عن عاصم",
            serverUrl = "https://server7.mp3quran.net/shur/"
        ),
        Reciter(
            id = "minshawi",
            name = "الشيخ محمد صديق المنشاوي",
            rewaya = "المصحف المرتل",
            serverUrl = "https://server10.mp3quran.net/minsh/"
        )
    )

    fun getMuezzinVoices(): List<MuezzinVoice> = listOf(
        MuezzinVoice(
            id = "makkah",
            name = "أذان الحرم المكي الشريف",
            location = "مكة المكرمة",
            sampleAudioUrl = "https://cdn.islamicfinder.org/athan/makkah.mp3"
        ),
        MuezzinVoice(
            id = "madinah",
            name = "أذان المسجد النبوي الشريف",
            location = "المدينة المنورة",
            sampleAudioUrl = "https://cdn.islamicfinder.org/athan/madinah.mp3"
        ),
        MuezzinVoice(
            id = "aqsa",
            name = "أذان المسجد الأقصى المبارك",
            location = "القدس الشريف",
            sampleAudioUrl = "https://cdn.islamicfinder.org/athan/aqsa.mp3"
        ),
        MuezzinVoice(
            id = "afasy",
            name = "أذان بصوت مشاري العفاسي",
            location = "الكويت",
            sampleAudioUrl = "https://cdn.islamicfinder.org/athan/afasy.mp3"
        )
    )

    fun getAudioUrlForSurah(reciter: Reciter, surahId: Int): String {
        val formattedId = String.format("%03d", surahId)
        return "${reciter.serverUrl}$formattedId.mp3"
    }
}
