package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerRepository
import com.example.ui.components.NavScreen
import com.example.ui.theme.CyanBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (NavScreen) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val prayerRepo = PrayerRepository()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Top Header: App Title & Location / Hijri Date
        item {
            HeaderCard(
                locationName = state.locationName,
                hijriDateFormatted = "${state.hijriDate.day} ${state.hijriDate.monthName} ${state.hijriDate.year} هـ",
                timeFormatted = state.currentTimeFormatted
            )
        }

        // Hero Next Prayer Card
        item {
            val nextPrayer = state.nextPrayerInfo
            if (nextPrayer != null) {
                HeroNextPrayerCard(
                    prayerType = nextPrayer.prayerType,
                    formattedTime = prayerRepo.formatTime(nextPrayer.prayerTimeMillis),
                    formattedCountdown = nextPrayer.formattedCountdown
                )
            }
        }

        // Prayer Schedule Horizontal Bar
        item {
            val schedule = state.prayerSchedule
            if (schedule != null) {
                val nextType = state.nextPrayerInfo?.prayerType
                val prayers = listOf(
                    PrayerType.FAJR to schedule.fajr,
                    PrayerType.DHUHR to schedule.dhuhr,
                    PrayerType.ASR to schedule.asr,
                    PrayerType.MAGHRIB to schedule.maghrib,
                    PrayerType.ISHA to schedule.isha
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prayers.forEach { (type, timeMillis) ->
                        val isNext = type == nextType
                        CompactPrayerPill(
                            prayerName = type.arName,
                            formattedTime = prayerRepo.formatTime(timeMillis),
                            isNext = isNext,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Khatma Progress Card
        item {
            KhatmaTrackerCard(
                khatmaState = state.khatmaState,
                lastReadSurahName = state.lastReadSurahName,
                onClick = { onNavigate(NavScreen.QURAN) }
            )
        }

        // Quick Grid Cards (Smart Misbaha & Hisn Al-Muslim)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GridShortcutCard(
                    iconText = "📿",
                    title = "السبحة الذكية",
                    subtitle = "انقر لبدء التسبيح والذكر",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavScreen.ADHKAR) }
                )
                GridShortcutCard(
                    iconText = "🤲",
                    title = "حصن المسلم",
                    subtitle = "١٥٤ فصلاً من الأذكار",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavScreen.ADHKAR) }
                )
            }
        }

        // Developer Credit Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تطوير وتنفيذ: عبدالرحمن حازم النتشة",
                    color = NeonCyanPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun HeaderCard(
    locationName: String,
    hijriDateFormatted: String,
    timeFormatted: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "أثر المسلم",
                color = NeonCyanPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = locationName.ifEmpty { "الرياض، المملكة العربية السعودية" },
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeFormatted,
                color = NeonCyanPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = hijriDateFormatted,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun HeroNextPrayerCard(
    prayerType: PrayerType,
    formattedTime: String,
    formattedCountdown: String
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Brush.horizontalGradient(listOf(com.example.ui.theme.NeonCyanPrimary, com.example.ui.theme.NeonPurpleAccent)), RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DarkSurfaceVariant,
                            Color(0xFF140D28),
                            DarkSurface
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الصلاة القادمة",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = prayerType.arName,
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(
                        text = "متبقي $formattedCountdown",
                        color = NeonCyanPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedTime,
                        color = NeonCyanPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "صوت الأذان: مكة المكرمة",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactPrayerPill(
    prayerName: String,
    formattedTime: String,
    isNext: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isNext) com.example.ui.theme.PurpleGlow else DarkSurface
    val borderModifier = if (isNext) {
        Modifier.border(1.5.dp, Brush.horizontalGradient(listOf(com.example.ui.theme.NeonCyanPrimary, com.example.ui.theme.NeonPurpleAccent)), RoundedCornerShape(16.dp))
    } else {
        Modifier.border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    }
    val labelColor = if (isNext) NeonCyanPrimary else TextSecondary
    val timeColor = if (isNext) TextPrimary else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .then(borderModifier)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = prayerName,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedTime,
                color = timeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KhatmaTrackerCard(
    khatmaState: com.example.data.model.KhatmaState,
    lastReadSurahName: String,
    onClick: () -> Unit
) {
    val progressPercent = khatmaState.progressPercentage.toInt()
    val fillFraction = (khatmaState.progressPercentage / 100f).coerceIn(0f, 1f)
    val subtitleText = if (khatmaState.currentPage > 0 && lastReadSurahName.isNotBlank()) {
        "التوقف الأخير: سورة $lastReadSurahName، صفحة ${khatmaState.currentPage}"
    } else if (khatmaState.currentPage > 0) {
        "التوقف الأخير: صفحة ${khatmaState.currentPage}"
    } else {
        "لم تبدأ الختمة بعد - اضغط للبدء"
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تتبع الختمة",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$progressPercent% مكتمل",
                        color = NeonCyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF101726))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (fillFraction < 0.02f && progressPercent > 0) 0.02f else fillFraction)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(com.example.ui.theme.NeonCyanPrimary, com.example.ui.theme.NeonPurpleAccent)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitleText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCyanPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📖", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun GridShortcutCard(
    iconText: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
            .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyanGlow),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
