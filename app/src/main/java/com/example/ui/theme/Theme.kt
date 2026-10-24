package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = SurfaceWhite,
  primaryContainer = SchoolBusYellowLight,
  onPrimaryContainer = NavyDark,
  secondary = SchoolBusYellow,
  onSecondary = NavyDark,
  secondaryContainer = SchoolBusYellowLight,
  onSecondaryContainer = SchoolBusYellowDark,
  tertiary = NavyLight,
  onTertiary = SurfaceWhite,
  background = BackgroundSlate,
  onBackground = TextPrimary,
  surface = SurfaceWhite,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceContainer,
  onSurfaceVariant = TextSecondary,
  outline = BorderSubtle,
  error = StatusRed,
  onError = SurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
  primary = SchoolBusYellow,
  onPrimary = NavyDark,
  primaryContainer = NavyLight,
  onPrimaryContainer = SchoolBusYellowLight,
  secondary = SchoolBusYellowAccent,
  onSecondary = NavyDark,
  tertiary = BorderSubtle,
  background = NavyDark,
  onBackground = SurfaceWhite,
  surface = NavyPrimary,
  onSurface = SurfaceWhite,
  surfaceVariant = NavyLight,
  onSurfaceVariant = BorderSubtle,
  outline = NavyMuted,
  error = StatusRed,
  onError = SurfaceWhite
)

@Composable
fun SafeRideTheme(
  darkTheme: Boolean = false, // Keep crisp clean light theme with navy structure by default
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
