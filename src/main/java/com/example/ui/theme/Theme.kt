package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AtharDarkColorScheme = darkColorScheme(
    primary = NeonCyanPrimary,
    onPrimary = Color(0xFF003547),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFCBEEFF),
    secondary = NeonCyanSecondary,
    onSecondary = Color.White,
    tertiary = NeonPurpleAccent,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun AtharTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AtharDarkColorScheme,
        typography = Typography,
        content = content
    )
}
