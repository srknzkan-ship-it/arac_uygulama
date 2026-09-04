package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AutoDrivingThemeMode {
    CYBER_NIGHT,
    AMBER_HUD_NIGHT,
    DAY_HIGH_CONTRAST
}

private val CyberNightColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382300),
    onSecondaryContainer = ElectricAmber,
    tertiary = ElectricBlue,
    background = ImmersiveVoid,
    onBackground = Color(0xFFF1F5F9),
    surface = ImmersiveCard,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ImmersiveCardSecondary,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = ImmersiveCardBorder,
    error = CrimsonRed
)

private val AmberHudNightColorScheme = darkColorScheme(
    primary = NightAmberPrimary,
    onPrimary = NightOledBlack,
    primaryContainer = NightAmberSurface,
    onPrimaryContainer = NightAmberSecondary,
    secondary = NightAmberSecondary,
    onSecondary = NightOledBlack,
    secondaryContainer = Color(0xFF261900),
    onSecondaryContainer = NightAmberPrimary,
    tertiary = NightRubyRed,
    background = NightOledBlack,
    onBackground = NightAmberPrimary,
    surface = NightAmberSurface,
    onSurface = NightAmberPrimary,
    surfaceVariant = Color(0xFF281C08),
    onSurfaceVariant = Color(0xFFCC8500),
    outline = NightAmberBorder,
    error = NightRubyRed
)

private val DayColorScheme = lightColorScheme(
    primary = DayAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    background = DayBackground,
    onBackground = DayTextPrimary,
    surface = DaySurface,
    onSurface = DayTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = DayTextSecondary,
    outline = DaySurfaceBorder,
    error = CrimsonRed
)

@Composable
fun AutoDriveTheme(
    mode: AutoDrivingThemeMode = AutoDrivingThemeMode.CYBER_NIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (mode) {
        AutoDrivingThemeMode.CYBER_NIGHT -> CyberNightColorScheme
        AutoDrivingThemeMode.AMBER_HUD_NIGHT -> AmberHudNightColorScheme
        AutoDrivingThemeMode.DAY_HIGH_CONTRAST -> DayColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
