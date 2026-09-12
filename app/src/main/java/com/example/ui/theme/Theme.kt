package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkRewardColorScheme =
  darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1700),
    primaryContainer = GoldDark,
    onPrimaryContainer = TextGold,
    secondary = GoldLight,
    onSecondary = Color(0xFF1F1800),
    secondaryContainer = DarkElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldGreenDark,
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF334155),
  )

private val LightRewardColorScheme =
  lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek dark gold theme as requested
  dynamicColor: Boolean = false, // Keep branded dark/gold reward theme consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkRewardColorScheme else LightRewardColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

