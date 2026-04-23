package com.rudra.savingbuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumDarkColorScheme = darkColorScheme(
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
    background = BackgroundMain,
    onBackground = TextPrimary,
    surface = BackgroundCard,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCardGlass,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = BorderMedium,
    inverseSurface = SurfaceLight,
    inverseOnSurface = BackgroundMain,
    inversePrimary = PrimaryGreenLight,
    surfaceTint = PrimaryGreen
)

private val PremiumLightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.White,
    tertiary = AccentPurple,
    onTertiary = Color.White,
    error = ExpenseRed,
    onError = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = BackgroundMain,
    surface = Color.White,
    onSurface = BackgroundMain,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SurfaceMedium,
    outline = BorderLight
)

@Composable
fun SavingBuddyTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = PremiumDarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundMain.toArgb()
            window.navigationBarColor = BackgroundMain.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}