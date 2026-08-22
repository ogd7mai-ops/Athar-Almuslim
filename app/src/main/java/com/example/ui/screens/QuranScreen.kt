package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Surah
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(viewModel: QuranViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val khatmaState by viewModel.khatmaState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: المصحف الشريف, 1: تتبع الختمة
    var fontSizeSp by remember { mutableFloatStateOf(20f) }
    var showKhatmaDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = NeonCyanPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NeonCyanPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "المصحف الشريف",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) NeonCyanPrimary else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "تتبع الختمة الذكية",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) NeonCyanPrimary else TextSecondary
                        )
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Quran Browser Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Input
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...", color = TextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyanPrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = NeonCyanPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredSurahs) { surah ->
                            SurahRowItem(
                                surah = surah,
                                onClick = { viewModel.selectSurah(surah) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            } else {
                // Khatma Tracker Tab
                KhatmaTrackerTab(
                    khatmaState = khatmaState,
                    onOpenDialog = { showKhatmaDialog = true },
                    onReset = { viewModel.resetKhatma() }
                )
            }
        }

        // Fullscreen Surah Reader Modal
        AnimatedVisibility(
            visible = uiState.selectedSurah != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            val surah = uiState.selectedSurah
            if (surah != null) {
                SurahReaderView(
                    surah = surah,
                    ayahs = uiState.activeAyahs,
                    fontSizeSp = fontSizeSp,
                    onFontSizeChange = { fontSizeSp = it },
                    onClose = { viewModel.clearSelectedSurah() },
                    onBookmarkPage = { page ->
                        viewModel.updateKhatmaCurrentPage(page)
                    }
                )
            }
        }

        // Khatma Update Dialog
        if (showKhatmaDialog) {
            UpdateKhatmaDialog(
                currentPage = khatmaState.currentPage,
                targetDaily = khatmaState.targetDailyPages,
                onDismiss = { showKhatmaDialog = false },
                onSave = { page, target ->
                    viewModel.saveKhatma(page, target)
                    showKhatmaDialog = false
                }
            )
        }
    }
}

@Composable
fun SurahRowItem(
    surah: Surah,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Number Octagon Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyanGlow)
                        .border(1.dp, NeonCyanPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.id}",
                        color = NeonCyanPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "سورة ${surah.nameArabic}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${surah.revelationType.arName} • ${surah.numberOfAyahs} آية • صفحة ${surah.startPage}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Text(
                text = surah.nameTranslation,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SurahReaderView(
    surah: Surah,
    ayahs: List<com.example.data.model.Ayah>,
    fontSizeSp: Float,
    onFontSizeChange: (Float) -> Unit,
    onClose: () -> Unit,
    onBookmarkPage: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = NeonCyanPrimary)
            }

            Text(
                text = "سورة ${surah.nameArabic}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { onBookmarkPage(surah.startPage) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyanGlow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("توقف هنا", color = NeonCyanPrimary, fontSize = 12.sp)
            }
        }

        // Font Size Adjuster
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("حجم الخط", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Slider(
                value = fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 16f..36f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyanPrimary,
                    activeTrackColor = NeonCyanPrimary
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Ayahs Container
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Separate View Component for Basmala (for all Surahs except Al-Fatihah #1 and At-Tawbah #9)
            if (surah.id != 1 && surah.id != 9) {
                item {
                    SurahBasmalaHeader(fontSizeSp = fontSizeSp)
                }
            }

            items(ayahs) { ayah ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = ayah.textUthmani,
                            color = TextPrimary,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.8f).sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الآية ﴿${ayah.numberInSurah}﴾",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "صفحة ${ayah.pageNumber} • الجزء ${ayah.juzNumber}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun KhatmaTrackerTab(
    khatmaState: com.example.data.model.KhatmaState,
    onOpenDialog: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyanPrimary, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = khatmaState.title,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تتبع مقدرا القراءة والتوقف اليومي",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        IconButton(onClick = onOpenDialog) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = NeonCyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "نسبة الإنجاز",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${String.format("%.1f", khatmaState.progressPercentage)}%",
                                color = GoldAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { khatmaState.progressPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = NeonCyanPrimary,
                            trackColor = DarkSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricBox(
                            label = "الصفحة الحالية",
                            value = "${khatmaState.currentPage} / 604",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "المقدار اليومي",
                            value = "${khatmaState.targetDailyPages} صفحات",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "الأيام المتبقية",
                            value = "${khatmaState.estimatedDaysLeft} يوم",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onOpenDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تحديث التوقف اليومي", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("البدء من جديد", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UpdateKhatmaDialog(
    currentPage: Int,
    targetDaily: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    var targetInput by remember { mutableStateOf(targetDaily.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("تحديث تقدم الختمة", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it },
                    label = { Text("الصفحة التي توقفت عندها (1-604)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyanPrimary
                    )
                )

                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    label = { Text("المقدار اليومي المفضل (صفحات)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyanPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = pageInput.toIntOrNull() ?: currentPage
                    val t = targetInput.toIntOrNull() ?: targetDaily
                    onSave(p.coerceIn(1, 604), t.coerceAtLeast(1))
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyanPrimary)
            ) {
                Text("حفظ التغييرات", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SurahBasmalaHeader(fontSizeSp: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(vertical = 18.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            color = GoldAccent,
            fontSize = (fontSizeSp + 3).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

