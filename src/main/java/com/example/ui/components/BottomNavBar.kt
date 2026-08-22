package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant

enum class NavScreen(val label: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Default.Home),
    QURAN("القرآن", Icons.Default.Book),
    ADHKAR("الأذكار", Icons.Default.SelfImprovement),
    AUDIO("الصوتيات", Icons.Default.GraphicEq),
    SETTINGS("الإعدادات", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentScreen: NavScreen,
    onScreenSelected: (NavScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, DarkBorder)
            .navigationBarsPadding()
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) NeonCyanPrimary else Color.White.copy(alpha = 0.5f),
                    label = "iconColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(com.example.ui.theme.NeonCyanGlow)
                                    .border(1.dp, com.example.ui.theme.CyanBorder, RoundedCornerShape(12.dp))
                            } else Modifier
                        )
                        .clickable { onScreenSelected(screen) }
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = screen.label,
                        color = iconColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
