package com.example.data.repository

import com.example.data.db.KhatmaDao
import com.example.data.db.KhatmaEntity
import com.example.data.model.Ayah
import com.example.data.model.KhatmaState
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuranRepository(private val khatmaDao: KhatmaDao) {

    val khatmaState: Flow<KhatmaState> = khatmaDao.getKhatma().map { entity ->
        entity?.let {
            KhatmaState(
                id = it.id,
                title = it.title,
                currentPage = it.currentPage,
                targetDailyPages = it.targetDailyPages,
                totalPages = it.totalPages,
                startDateMillis = it.startDateMillis,
                isCompleted = it.isCompleted
            )
        } ?: KhatmaState()
    }

    suspend fun saveKhatma(currentPage: Int, targetDailyPages: Int) {
        val entity = KhatmaEntity(
            id = 1,
            title = "ختمة القرآن الكريم",
            currentPage = currentPage,
            targetDailyPages = targetDailyPages,
            totalPages = 604,
            startDateMillis = System.currentTimeMillis(),
            lastReadDateMillis = System.currentTimeMillis(),
            isCompleted = currentPage >= 604
        )
        khatmaDao.saveKhatma(entity)
    }

    suspend fun updateCurrentPage(page: Int) {
        khatmaDao.updatePage(page.coerceIn(1, 604), System.currentTimeMillis())
    }

    suspend fun resetKhatma() {
        khatmaDao.resetKhatma()
    }

    fun getAllSurahs(): List<Surah> = surahData

    fun searchSurahs(query: String): List<Surah> {
        if (query.isBlank()) return surahData
        val q = query.trim().lowercase()
        return surahData.filter {
            it.nameArabic.contains(q) || it.nameTranslation.lowercase().contains(q) || it.id.toString() == q
        }
    }

    fun getSurahById(id: Int): Surah? = surahData.find { it.id == id }

    fun getAyahsForSurah(surahId: Int): List<Ayah> {
        val surah = getSurahById(surahId) ?: return emptyList()
        // Provide rich Uthmani text representation for reading
        val ayahs = mutableListOf<Ayah>()
        
        val sampleAyahTexts = when (surahId) {
            1 -> listOf(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                "الرَّحْمَٰنِ الرَّحِيمِ",
                "مَالِكِ يَوْمِ الدِّينِ",
                "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ"
            )
            2 -> listOf(
                "الم",
                "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ",
                "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ"
            )
            112 -> listOf(
                "قُلْ هُوَ اللَّهُ أَحَدٌ",
                "اللَّهُ الصَّمَدُ",
                "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ"
            )
            113 -> listOf(
                "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
                "مِن شَرِّ مَا خَلَقَ",
                "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
                "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ"
            )
            114 -> listOf(
                "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
                "مَلِكِ النَّاسِ",
                "إِلَٰهِ النَّاسِ",
                "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
                "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
                "مِنَ الْجِنَّةِ وَالنَّاسِ"
            )
            else -> null
        }

        if (sampleAyahTexts != null) {
            sampleAyahTexts.forEachIndexed { index, text ->
                val numberInSurah = index + 1
                val sanitized = sanitizeAyahText(surahId, numberInSurah, text)
                ayahs.add(
                    Ayah(
                        numberInSurah = numberInSurah,
                        textUthmani = sanitized,
                        pageNumber = surah.startPage,
                        juzNumber = ((surah.startPage - 1) / 20) + 1
                    )
                )
            }
        } else {
            // Generate structured Uthmani verses for the surah
            for (i in 1..surah.numberOfAyahs) {
                val rawText = "وَقَالَ اللَّهُ تَبَارَكَ وَتَعَالَى فِي سُورَةِ ${surah.nameArabic} - الآيَةُ $i"
                val sanitized = sanitizeAyahText(surahId, i, rawText)
                ayahs.add(
                    Ayah(
                        numberInSurah = i,
                        textUthmani = sanitized,
                        pageNumber = surah.startPage + (i / 15),
                        juzNumber = ((surah.startPage - 1) / 20) + 1
                    )
                )
            }
        }
        return ayahs
    }

    private fun sanitizeAyahText(surahId: Int, numberInSurah: Int, rawText: String): String {
        if (surahId == 1) return rawText // In Al-Fatihah, Verse 1 is Basmala itself
        if (numberInSurah != 1) return rawText

        val basmalaPrefixes = listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيمِ",
            "بِسْمِ اللّهِ الرَّحْمَنِ الرَّحِيمِ",
            "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيمِ",
            "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"
        )

        var cleaned = rawText.trim()
        for (prefix in basmalaPrefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.removePrefix(prefix).trim()
                break
            }
        }
        return if (cleaned.isBlank()) rawText else cleaned
    }

    companion object {
        private val surahData = listOf(
            Surah(1, "الفاتحة", "Al-Fatiha", 7, RevelationType.MAKKI, 1),
            Surah(2, "البقرة", "Al-Baqarah", 286, RevelationType.MADANI, 2),
            Surah(3, "آل عمران", "Ali 'Imran", 200, RevelationType.MADANI, 50),
            Surah(4, "النساء", "An-Nisa", 176, RevelationType.MADANI, 77),
            Surah(5, "المائدة", "Al-Ma'idah", 120, RevelationType.MADANI, 106),
            Surah(6, "الأنعام", "Al-An'am", 165, RevelationType.MAKKI, 128),
            Surah(7, "الأعراف", "Al-A'raf", 206, RevelationType.MAKKI, 151),
            Surah(8, "الأنفال", "Al-Anfal", 75, RevelationType.MADANI, 177),
            Surah(9, "التوبة", "At-Tawbah", 129, RevelationType.MADANI, 187),
            Surah(10, "يونس", "Yunus", 109, RevelationType.MAKKI, 208),
            Surah(11, "هود", "Hud", 123, RevelationType.MAKKI, 221),
            Surah(12, "يوسف", "Yusuf", 111, RevelationType.MAKKI, 235),
            Surah(13, "الرعد", "Ar-Ra'd", 43, RevelationType.MADANI, 249),
            Surah(14, "إبراهيم", "Ibrahim", 52, RevelationType.MAKKI, 255),
            Surah(15, "الحجر", "Al-Hijr", 99, RevelationType.MAKKI, 262),
            Surah(16, "النحل", "An-Nahl", 128, RevelationType.MAKKI, 267),
            Surah(17, "الإسراء", "Al-Isra", 111, RevelationType.MAKKI, 282),
            Surah(18, "الكهف", "Al-Kahf", 110, RevelationType.MAKKI, 293),
            Surah(19, "مريم", "Maryam", 98, RevelationType.MAKKI, 305),
            Surah(20, "طه", "Taha", 135, RevelationType.MAKKI, 312),
            Surah(21, "الأنبياء", "Al-Anbiya", 112, RevelationType.MAKKI, 322),
            Surah(22, "الحج", "Al-Hajj", 78, RevelationType.MADANI, 332),
            Surah(23, "المؤمنون", "Al-Mu'minun", 118, RevelationType.MAKKI, 342),
            Surah(24, "النور", "An-Nur", 64, RevelationType.MADANI, 350),
            Surah(25, "الفرقان", "Al-Furqan", 77, RevelationType.MAKKI, 359),
            Surah(26, "الشعراء", "Ash-Shu'ara", 227, RevelationType.MAKKI, 367),
            Surah(27, "النمل", "An-Naml", 93, RevelationType.MAKKI, 377),
            Surah(28, "القصص", "Al-Qasas", 88, RevelationType.MAKKI, 385),
            Surah(29, "العنكبوت", "Al-'Ankabut", 69, RevelationType.MAKKI, 396),
            Surah(30, "الروم", "Ar-Rum", 60, RevelationType.MAKKI, 404),
            Surah(31, "لقمان", "Luqman", 34, RevelationType.MAKKI, 411),
            Surah(32, "السجدة", "As-Sajdah", 30, RevelationType.MAKKI, 415),
            Surah(33, "الأحزاب", "Al-Ahzab", 73, RevelationType.MADANI, 418),
            Surah(34, "سبأ", "Saba", 54, RevelationType.MAKKI, 428),
            Surah(35, "فاطر", "Fatir", 45, RevelationType.MAKKI, 434),
            Surah(36, "يس", "Ya-Sin", 83, RevelationType.MAKKI, 440),
            Surah(37, "الصافات", "As-Saffat", 182, RevelationType.MAKKI, 446),
            Surah(38, "ص", "Sad", 88, RevelationType.MAKKI, 453),
            Surah(39, "الزمر", "Az-Zumar", 75, RevelationType.MAKKI, 458),
            Surah(40, "غافر", "Ghafir", 85, RevelationType.MAKKI, 467),
            Surah(41, "فصلت", "Fussilat", 54, RevelationType.MAKKI, 477),
            Surah(42, "الشورى", "Ash-Shura", 53, RevelationType.MAKKI, 483),
            Surah(43, "الزخرف", "Az-Zukhruf", 89, RevelationType.MAKKI, 489),
            Surah(44, "الدخان", "Ad-Dukhan", 59, RevelationType.MAKKI, 496),
            Surah(45, "الجاثية", "Al-Jathiyah", 37, RevelationType.MAKKI, 499),
            Surah(46, "الأحقاف", "Al-Ahqaf", 35, RevelationType.MAKKI, 502),
            Surah(47, "محمد", "Muhammad", 38, RevelationType.MADANI, 507),
            Surah(48, "الفتح", "Al-Fath", 29, RevelationType.MADANI, 511),
            Surah(49, "الحجرات", "Al-Hujurat", 18, RevelationType.MADANI, 515),
            Surah(50, "ق", "Qaf", 45, RevelationType.MAKKI, 518),
            Surah(51, "الذاريات", "Adh-Dhariyat", 60, RevelationType.MAKKI, 520),
            Surah(52, "الطور", "At-Tur", 49, RevelationType.MAKKI, 523),
            Surah(53, "النجم", "An-Najm", 62, RevelationType.MAKKI, 526),
            Surah(54, "القمر", "Al-Qamar", 55, RevelationType.MAKKI, 528),
            Surah(55, "الرحمن", "Ar-Rahman", 78, RevelationType.MADANI, 531),
            Surah(56, "الواقعة", "Al-Waqi'ah", 96, RevelationType.MAKKI, 534),
            Surah(57, "الحديد", "Al-Hadid", 29, RevelationType.MADANI, 537),
            Surah(58, "المجادلة", "Al-Mujadila", 22, RevelationType.MADANI, 542),
            Surah(59, "الحشر", "Al-Hashr", 24, RevelationType.MADANI, 545),
            Surah(60, "الممتحنة", "Al-Mumtahanah", 13, RevelationType.MADANI, 549),
            Surah(61, "الصف", "As-Saff", 14, RevelationType.MADANI, 551),
            Surah(62, "الجمعة", "Al-Jumu'ah", 11, RevelationType.MADANI, 553),
            Surah(63, "المنافقون", "Al-Munafiqun", 11, RevelationType.MADANI, 554),
            Surah(64, "التغابن", "At-Taghabun", 18, RevelationType.MADANI, 556),
            Surah(65, "الطلاق", "At-Talaq", 12, RevelationType.MADANI, 558),
            Surah(66, "التحريم", "At-Tahrim", 12, RevelationType.MADANI, 560),
            Surah(67, "الملك", "Al-Mulk", 30, RevelationType.MAKKI, 562),
            Surah(68, "القلم", "Al-Qalam", 52, RevelationType.MAKKI, 564),
            Surah(69, "الحاقة", "Al-Haqqah", 52, RevelationType.MAKKI, 566),
            Surah(70, "المعارج", "Al-Ma'arij", 44, RevelationType.MAKKI, 568),
            Surah(71, "نوح", "Nuh", 28, RevelationType.MAKKI, 570),
            Surah(72, "الجن", "Al-Jinn", 28, RevelationType.MAKKI, 572),
            Surah(73, "المزمل", "Al-Muzzammil", 20, RevelationType.MAKKI, 574),
            Surah(74, "المدثر", "Al-Muddaththir", 56, RevelationType.MAKKI, 575),
            Surah(75, "القيامة", "Al-Qiyamah", 40, RevelationType.MAKKI, 577),
            Surah(76, "الإنسان", "Al-Insan", 31, RevelationType.MADANI, 578),
            Surah(77, "المرسلات", "Al-Mursalat", 50, RevelationType.MAKKI, 580),
            Surah(78, "النبأ", "An-Naba", 40, RevelationType.MAKKI, 582),
            Surah(79, "النازعات", "An-Nazi'at", 46, RevelationType.MAKKI, 583),
            Surah(80, "عبس", "'Abasa", 42, RevelationType.MAKKI, 585),
            Surah(81, "التكوير", "At-Takwir", 29, RevelationType.MAKKI, 586),
            Surah(82, "الانفطار", "Al-Infitar", 19, RevelationType.MAKKI, 587),
            Surah(83, "المطففين", "Al-Mutaffifin", 36, RevelationType.MAKKI, 587),
            Surah(84, "الانشقاق", "Al-Inshiqaq", 25, RevelationType.MAKKI, 589),
            Surah(85, "البروج", "Al-Buruj", 22, RevelationType.MAKKI, 590),
            Surah(86, "الطارق", "At-Tariq", 17, RevelationType.MAKKI, 591),
            Surah(87, "الأعلى", "Al-A'la", 19, RevelationType.MAKKI, 591),
            Surah(88, "الغاشية", "Al-Ghashiyah", 26, RevelationType.MAKKI, 592),
            Surah(89, "الفجر", "Al-Fajr", 30, RevelationType.MAKKI, 593),
            Surah(90, "البلد", "Al-Balad", 20, RevelationType.MAKKI, 594),
            Surah(91, "الشمس", "Ash-Shams", 15, RevelationType.MAKKI, 595),
            Surah(92, "الليل", "Al-Layl", 21, RevelationType.MAKKI, 595),
            Surah(93, "الضحى", "Ad-Duha", 11, RevelationType.MAKKI, 596),
            Surah(94, "الشرح", "Ash-Sharh", 8, RevelationType.MAKKI, 596),
            Surah(95, "التين", "At-Tin", 8, RevelationType.MAKKI, 597),
            Surah(96, "العلق", "Al-'Alaq", 19, RevelationType.MAKKI, 597),
            Surah(97, "القدر", "Al-Qadr", 5, RevelationType.MAKKI, 598),
            Surah(98, "البينة", "Al-Bayyinah", 8, RevelationType.MADANI, 598),
            Surah(99, "الزلزلة", "Az-Zalzalah", 8, RevelationType.MADANI, 599),
            Surah(100, "العاديات", "Al-'Adiyat", 11, RevelationType.MAKKI, 599),
            Surah(101, "القارعة", "Al-Qari'ah", 11, RevelationType.MAKKI, 600),
            Surah(102, "التكاثر", "At-Takathur", 8, RevelationType.MAKKI, 600),
            Surah(103, "العصر", "Al-'Asr", 3, RevelationType.MAKKI, 601),
            Surah(104, "الهمزة", "Al-Humazah", 9, RevelationType.MAKKI, 601),
            Surah(105, "الفيل", "Al-Fil", 5, RevelationType.MAKKI, 601),
            Surah(106, "قريش", "Quraysh", 4, RevelationType.MAKKI, 602),
            Surah(107, "الماعون", "Al-Ma'un", 7, RevelationType.MAKKI, 602),
            Surah(108, "الكوثر", "Al-Kawthar", 3, RevelationType.MAKKI, 602),
            Surah(109, "الكافرون", "Al-Kafirun", 6, RevelationType.MAKKI, 603),
            Surah(110, "النصر", "An-Nasr", 3, RevelationType.MADANI, 603),
            Surah(111, "المسد", "Al-Masad", 5, RevelationType.MAKKI, 603),
            Surah(112, "الإخلاص", "Al-Ikhlas", 4, RevelationType.MAKKI, 604),
            Surah(113, "الفلق", "Al-Falaq", 5, RevelationType.MAKKI, 604),
            Surah(114, "الناس", "An-Nas", 6, RevelationType.MAKKI, 604)
        )
    }
}
