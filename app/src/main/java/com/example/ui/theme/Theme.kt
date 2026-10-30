package com.example.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = GlassDarkPrimary,
    secondary = GlassDarkSecondary,
    tertiary = GlassDarkPrimary,
    background = GlassDarkBackgroundStart,
    surface = GlassDarkCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = GlassDarkTextPrimary,
    onSurface = GlassDarkTextPrimary,
    surfaceVariant = Color(0x22FFFFFF),
    onSurfaceVariant = GlassDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = GlassLightPrimary,
    secondary = GlassLightSecondary,
    tertiary = GlassLightPrimary,
    background = GlassLightBackgroundStart,
    surface = GlassLightCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = GlassLightTextPrimary,
    onSurface = GlassLightTextPrimary,
    surfaceVariant = Color(0x33FFFFFF),
    onSurfaceVariant = GlassLightTextSecondary
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
