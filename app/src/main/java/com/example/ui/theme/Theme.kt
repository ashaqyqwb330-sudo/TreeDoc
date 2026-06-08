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

private val SophisticatedColorScheme = darkColorScheme(
    primary = SophisticatedDarkPrimary,
    onPrimary = SophisticatedDarkOnPrimary,
    primaryContainer = SophisticatedDarkSurface,
    onPrimaryContainer = SophisticatedDarkTextPrimary,
    secondary = SophisticatedDarkPrimary,
    onSecondary = SophisticatedDarkOnPrimary,
    background = SophisticatedDarkBg,
    onBackground = SophisticatedDarkTextPrimary,
    surface = SophisticatedDarkSurface,
    onSurface = SophisticatedDarkTextPrimary,
    surfaceVariant = SophisticatedDarkSurface,
    onSurfaceVariant = SophisticatedDarkTextSecondary,
    outline = SophisticatedDarkOutline,
    outlineVariant = SophisticatedDarkOutline.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = SophisticatedColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
