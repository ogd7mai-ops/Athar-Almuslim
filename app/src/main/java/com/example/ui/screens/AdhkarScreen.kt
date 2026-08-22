package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrItem
import com.example.data.model.HisnCategory
import com.example.data.model.TasbeehOption
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HisnViewModel

@Composable
fun AdhkarScreen(viewModel: HisnViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = DarkSurface,
            contentColor = NeonCyanPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = NeonCyanPrimary
                )
            }
        ) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("الأذكار اليومية", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("حصن المسلم", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = state.selectedTab == 2,
                onClick = { viewModel.selectTab(2) },
                text = { Text("السبحة الإلكترونية", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            )
        }

        when (state.selectedTab) {
            0 -> DailyAdhkarTab(viewModel, state)
            1 -> HisnCategoriesTab(viewModel, state)
            2 -> ElectronicTasbeehTab(viewModel, state)
        }
    }
}

@Composable
fun DailyAdhkarTab(
    viewModel: HisnViewModel,
    state: com.example.ui.viewmodel.HisnUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Adhkar Type Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdhkarTypePill(
                title = "أذكار الصباح",
                isSelected = state.selectedDhikrType == "MORNING",
                onClick = { viewModel.selectDhikrType("MORNING") },
                modifier = Modifier.weight(1f)
            )
            AdhkarTypePill(
                title = "أذكار المساء",
                isSelected = state.selectedDhikrType == "EVENING",
                onClick = { viewModel.selectDhikrType("EVENING") },
                modifier = Modifier.weight(1f)
            )
            AdhkarTypePill(
                title = "أذكار بعد الصلاة",
                isSelected = state.selectedDhikrType == "AFTER_PRAYER",
                onClick = { viewModel.selectDhikrType("AFTER_PRAYER") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.activeDhikrList) { item ->
                val countKey = "${state.selectedDhikrType}_${item.id}"
                val currentCount = state.dhikrCounts[countKey] ?: 0
                DhikrCard(
                    item = item,
                    currentCount = currentCount,
                    onIncrement = {
                        viewModel.incrementDhikrCount(countKey, item.targetCount)
                    },
                    onReset = {
                        viewModel.resetDhikrCount(countKey)
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AdhkarTypePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) NeonCyanPrimary else DarkSurface
    val textColor = if (isSelected) Color.Black else TextSecondary
    val border = if (isSelected) NeonCyanPrimary else DarkBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DhikrCard(
    item: DhikrItem,
    currentCount: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    val isCompleted = currentCount >= item.targetCount
    val cardBg = if (isCompleted) DarkSurfaceVariant else DarkSurface
    val borderColor = if (isCompleted) GoldAccent else DarkBorder

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { if (!isCompleted) onIncrement() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.text,
                color = TextPrimary,
                fontSize = 17.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.virtue.isNotEmpty()) {
                Text(
                    text = "💡 ${item.virtue}",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = TextSecondary)
                }

                // Interactive Tap Button Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isCompleted) GoldAccent else NeonCyanPrimary)
                        .clickable { onIncrement() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مكتمل ($currentCount / ${item.targetCount})",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "اضغط للتكرار ($currentCount / ${item.targetCount})",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HisnCategoriesTab(
    viewModel: HisnViewModel,
    state: com.example.ui.viewmodel.HisnUiState
) {
    val selectedCat = state.selectedHisnCategory

    if (selectedCat != null) {
        // Detailed Hisn Chapter View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.selectHisnCategory(null) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← العودة للأبواب", color = NeonCyanPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = selectedCat.title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(selectedCat.items) { dhikr ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = dhikr.text,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (dhikr.virtue.isNotEmpty()) {
                                Text(
                                    text = "💡 ${dhikr.virtue}",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    } else {
        // Hisn Chapters Grid / List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "أبواب كتاب حصن المسلم",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.hisnCategories) { cat ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .clickable { viewModel.selectHisnCategory(cat) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = cat.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cat.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${cat.items.size} أذكار",
                            color = NeonCyanPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElectronicTasbeehTab(
    viewModel: HisnViewModel,
    state: com.example.ui.viewmodel.HisnUiState
) {
    val selectedOption = state.selectedTasbeeh ?: state.tasbeehOptions.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Phrase Picker Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.tasbeehOptions) { option ->
                val isSel = option.id == selectedOption?.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) NeonCyanPrimary else DarkSurface)
                        .border(1.dp, if (isSel) NeonCyanPrimary else DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectTasbeehOption(option) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = option.text,
                        color = if (isSel) Color.Black else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedOption != null) {
            Text(
                text = selectedOption.text,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "✨ ${selectedOption.virtue}",
                color = GoldAccent,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Large Glowing Touch Counter Button
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .border(4.dp, NeonCyanPrimary, CircleShape)
                .clickable { viewModel.incrementTasbeeh() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${state.tasbeehCount}",
                    color = NeonCyanPrimary,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "المجموع: ${state.totalTasbeehSession}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("انقر للتسبيح", color = GoldAccent, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { viewModel.resetTasbeeh() },
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("إعادة ضبط العداد", color = TextSecondary)
        }
    }
}
