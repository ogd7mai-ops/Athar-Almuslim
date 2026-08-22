package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Reciter
import com.example.data.repository.QuranRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AudioViewModel
import java.util.Locale

@Composable
fun AudioScreen(viewModel: AudioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val allSurahs = QuranRepository(
        com.example.data.db.AtharDatabase.getInstance(androidx.compose.ui.platform.LocalContext.current).khatmaDao()
    ).getAllSurahs()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "مكتبة القرآن التلاوات الصوتية",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reciter Selector Row
            Text(
                text = "اختر القارئ المفضل:",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.reciters) { reciter ->
                    val isSelected = reciter.id == state.selectedReciter?.id
                    ReciterChip(
                        reciter = reciter,
                        isSelected = isSelected,
                        onClick = { viewModel.selectReciter(reciter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Surahs List for Audio
            Text(
                text = "السور المتاحة للاستماع:",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(allSurahs) { surah ->
                    val isCurrent = surah.id == state.currentSurahId
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) DarkSurfaceVariant else DarkSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isCurrent) NeonCyanPrimary else DarkBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.playSurah(surah.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) NeonCyanPrimary else DarkBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCurrent && state.isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color.Black else NeonCyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "سورة ${surah.nameArabic}",
                                        color = if (isCurrent) NeonCyanPrimary else TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${surah.revelationType.arName} • ${surah.numberOfAyahs} آية",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isCurrent && state.isPlaying) {
                                Text(
                                    text = "جاري التشغيل...",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        // Bottom Sticky Audio Player Bar
        if (state.selectedReciter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, NeonCyanPrimary, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${state.currentSurahNameAr}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.selectedReciter?.name ?: "",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (state.isBuffering) {
                            CircularProgressIndicator(
                                color = NeonCyanPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyanPrimary)
                            ) {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل/إيقاف",
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                    // Seekbar
                    if (state.durationMillis > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = state.currentPositionMillis.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toInt()) },
                            valueRange = 0f..state.durationMillis.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyanPrimary,
                                activeTrackColor = NeonCyanPrimary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatMs(state.currentPositionMillis),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = formatMs(state.durationMillis),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReciterChip(
    reciter: Reciter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) NeonCyanPrimary else DarkSurface
    val textColor = if (isSelected) Color.Black else TextPrimary
    val border = if (isSelected) NeonCyanPrimary else DarkBorder

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = reciter.name,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = reciter.rewaya,
                color = if (isSelected) Color.DarkGray else TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale("ar"), "%02d:%02d", min, sec)
}
