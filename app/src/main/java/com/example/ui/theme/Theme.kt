package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = Indigo,
    onPrimary = SoftSurface,
    primaryContainer = Sky.copy(alpha = 0.16f),
    onPrimaryContainer = Night,
    secondary = Mint,
    onSecondary = SoftSurface,
    secondaryContainer = Mint.copy(alpha = 0.18f),
    onSecondaryContainer = Night,
    tertiary = Amber,
    onTertiary = Night,
    background = Cloud,
    onBackground = Night,
    surface = SoftSurface,
    onSurface = SoftText,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = SoftTextSecondary,
    outline = SoftTextSecondary.copy(alpha = 0.4f),
    error = SoftError,
    onError = SoftSurface
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = Indigo,
    onPrimary = SoftSurface,
    primaryContainer = Sky,
    onPrimaryContainer = SoftSurface,
    secondary = Mint,
    onSecondary = Night,
    secondaryContainer = Mint.copy(alpha = 0.24f),
    onSecondaryContainer = SoftSurface,
    tertiary = Amber,
    onTertiary = Night,
    background = Night,
    onBackground = SoftSurface,
    surface = SoftSurfaceDark,
    onSurface = SoftSurface,
    surfaceVariant = SoftSurfaceVariantDark,
    onSurfaceVariant = SoftTextSecondary,
    outline = SoftTextSecondary.copy(alpha = 0.45f),
    error = SoftError,
    onError = SoftSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
