package com.rudra.savingbuddy.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// PREMIUM DARK FINTECH THEME
// ============================================

// Primary Colors
val PrimaryGreen = Color(0xFF22C55E)
val PrimaryGreenLight = Color(0xFF4ADE80)
val PrimaryGreenDark = Color(0xFF16A34A)

val ExpenseRed = Color(0xFFEF4444)
val ExpenseRedLight = Color(0xFFF87171)
val ExpenseRedDark = Color(0xFFDC2626)

val WarningOrange = Color(0xFFF59E0B)
val WarningOrangeLight = Color(0xFFFBBF24)

// Background Colors
val BackgroundMain = Color(0xFF0B1220)
val BackgroundCard = Color(0xFF111827)
val BackgroundCardGlass = Color(0xFF1E293B)
val BackgroundElevated = Color(0xFF1F2937)

// Surface Colors
val SurfaceLight = Color(0xFFE2E8F0)
val SurfaceMedium = Color(0xFF94A3B8)
val SurfaceDark = Color(0xFF475569)

// Accent Colors
val AccentTeal = Color(0xFF14B8A6)
val AccentCyan = Color(0xFF06B6D4)
val AccentPurple = Color(0xFF8B5CF6)

// Text Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Border Colors
val BorderLight = Color(0xFF334155)
val BorderMedium = Color(0xFF1E293B)
val BorderGlow = Color(0xFF22C55E)

// Legacy Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Green500 = Color(0xFF4CAF50)
val Green700 = Color(0xFF388E3C)
val Red500 = Color(0xFFF44336)
val Red700 = Color(0xFFD32F2F)
val Orange500 = Color(0xFFFF9800)
val Blue500 = Color(0xFF2196F3)

val IncomeGreen = Color(0xFF22C55E)
val SavingsBlue = Color(0xFF3B82F6)

// Category Colors
val FoodColor = Color(0xFFF97316)
val TransportColor = Color(0xFF3B82F6)
val BillsColor = Color(0xFFEAB308)
val ShoppingColor = Color(0xFF8B5CF6)
val EntertainmentColor = Color(0xFFEC4899)
val HealthColor = Color(0xFF14B8A6)
val EducationColor = Color(0xFF6366F1)
val GiftsColor = Color(0xFFF472B6)
val TravelColor = Color(0xFF06B6D4)
val SubscriptionsColor = Color(0xFF7C3AED)
val RentColor = Color(0xFFEF4444)
val OthersColor = Color(0xFF64748B)

// Account Colors
val bKashColor = Color(0xFF7000FF)
val NagadColor = Color(0xFFFF6B00)
val BankColor = Color(0xFF0066FF)
val CashColor = Color(0xFF22C55E)

// ============================================
// THEME EXTENSIONS
// ============================================

val Color.glass: Color
    get() = this.copy(alpha = 0.7f)

val Color.glassLight: Color
    get() = this.copy(alpha = 0.5f)

val Color.glow: Color
    get() = this.copy(alpha = 0.3f)

// Gradients
val greenGradient = listOf(PrimaryGreen, AccentTeal)
val tealGradient = listOf(AccentTeal, AccentCyan)
val purpleGradient = listOf(AccentPurple, Color(0xFF6366F1))
val redGradient = listOf(ExpenseRed, ExpenseRedLight)
val orangeGradient = listOf(WarningOrange, Color(0xFFFBBF24))

val cardGradient = listOf(
    Color(0xFF1E293B),
    Color(0xFF111827)
)

val accentCardGradient = listOf(
    PrimaryGreen.copy(alpha = 0.2f),
    AccentTeal.copy(alpha = 0.1f)
)