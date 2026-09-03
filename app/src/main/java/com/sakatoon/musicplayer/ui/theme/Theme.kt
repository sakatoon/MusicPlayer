package com.sakatoon.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = LightGray,
    onSecondary = Black,
    tertiary = MediumGray,
    onTertiary = White,
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = MediumGray,
    onSurfaceVariant = LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = MediumGray,
    onSecondary = White,
    tertiary = LightGray,
    onTertiary = Black,
    background = White,
    onBackground = Black,
    surface = Color(0xFFF5F5F5), // Slightly off-white for surface
    onSurface = Black,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Black
)

@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = true, // Force dark theme by default as per request "negro gris y blanco" fitting a dark aesthetic usually
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}