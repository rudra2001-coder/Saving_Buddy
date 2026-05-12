package com.rudra.savingbuddy.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val AmoledDarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentTeal.copy(alpha = 0.3f),
    onSecondaryContainer = AccentTeal,
    tertiary = AccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = AccentPurple.copy(alpha = 0.3f),
    onTertiaryContainer = AccentPurple,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRed.copy(alpha = 0.2f),
    onErrorContainer = ExpenseRed,
    background = Color(0xFF000000),
    onBackground = TextPrimary,
    surface = Color(0xFF000000),
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF0A0A0A),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF1A1A2E),
    outlineVariant = Color(0xFF0D0D1A),
    inverseSurface = SurfaceLight,
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = PrimaryGreenLight,
    surfaceTint = PrimaryGreen
)

enum class ThemeMode {
    LIGHT, DARK, AMOLED, SCHEDULED
}

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.DARK,
    val scheduleStartHour: Int = 18,
    val scheduleEndHour: Int = 6,
    val useAmoled: Boolean = false,
    val followSystem: Boolean = false
)
