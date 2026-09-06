package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedicalTealLight,
    onPrimary = MedicalTealDark,
    primaryContainer = MedicalTeal,
    onPrimaryContainer = MedicalTealContainer,
    secondary = SlateSecondary,
    onSecondary = SlateSurfaceLight,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalTeal,
    onPrimary = Color.White,
    primaryContainer = MedicalTealContainer,
    onPrimaryContainer = MedicalOnTealContainer,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    background = SlateBackgroundLight,
    surface = SlateSurfaceLight,
    onBackground = SlatePrimary,
    onSurface = SlatePrimary,
    outline = SlateBorderLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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
