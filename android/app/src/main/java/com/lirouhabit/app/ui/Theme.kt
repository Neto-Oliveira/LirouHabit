package com.lirouhabit.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandRed = Color(0xFFE53935)
val BrandAmber = Color(0xFFFFB300)
val VictoryGreen = Color(0xFF43A047)
val DefeatRed = Color(0xFFE57373)

private val LirouColors = darkColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    secondary = BrandAmber,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFCF6679)
)

@Composable
fun LirouHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LirouColors, content = content)
}
