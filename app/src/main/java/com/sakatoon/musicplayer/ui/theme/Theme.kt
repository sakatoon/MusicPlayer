package com.sakatoon.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class MusicTheme(
    val storageValue: String,
    val displayName: String,
    val accent: Color
) {
    DEFAULT("default", "Clásico", Color(0xFF03BDE8)),
    OCEAN("ocean", "Océano", Color(0xFF38BDF8)),
    FOREST("forest", "Bosque", Color(0xFF4ADE80)),
    SUNSET("sunset", "Atardecer", Color(0xFFFF9F43)),
    LIGHT("light", "Modo claro", Color(0xFF0284C7));

    companion object {
        fun fromStorage(value: String?): MusicTheme = values().firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}

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
    primary = Color(0xFF0284C7),
    onPrimary = White,
    secondary = Color(0xFF0369A1),
    onSecondary = White,
    tertiary = Color(0xFF0EA5E9),
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = Color(0xFFF5F5F5), // Slightly off-white for surface
    onSurface = Black,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Black
)

private fun darkColorSchemeFor(theme: MusicTheme) = when (theme) {
    MusicTheme.DEFAULT -> DarkColorScheme
    MusicTheme.OCEAN -> darkColorScheme(
        primary = Color(0xFF38BDF8), onPrimary = Color(0xFF002333),
        secondary = Color(0xFF7DD3FC), onSecondary = Color(0xFF002333),
        tertiary = Color(0xFF60A5FA), onTertiary = Color(0xFF001B3D),
        background = Color(0xFF071521), onBackground = Color(0xFFE0F2FE),
        surface = Color(0xFF0D2233), onSurface = Color(0xFFE0F2FE),
        surfaceVariant = Color(0xFF173A50), onSurfaceVariant = Color(0xFFB6D7E8)
    )
    MusicTheme.FOREST -> darkColorScheme(
        primary = Color(0xFF4ADE80), onPrimary = Color(0xFF00210B),
        secondary = Color(0xFF86EFAC), onSecondary = Color(0xFF00210B),
        tertiary = Color(0xFFA3E635), onTertiary = Color(0xFF162000),
        background = Color(0xFF0A170E), onBackground = Color(0xFFE3F5E7),
        surface = Color(0xFF12251A), onSurface = Color(0xFFE3F5E7),
        surfaceVariant = Color(0xFF1D3A28), onSurfaceVariant = Color(0xFFB8D8BE)
    )
    MusicTheme.SUNSET -> darkColorScheme(
        primary = Color(0xFFFF9F43), onPrimary = Color(0xFF321300),
        secondary = Color(0xFFFFC078), onSecondary = Color(0xFF321300),
        tertiary = Color(0xFFFF6B6B), onTertiary = Color(0xFF3B0000),
        background = Color(0xFF1D100C), onBackground = Color(0xFFFFEDE4),
        surface = Color(0xFF2B1812), onSurface = Color(0xFFFFEDE4),
        surfaceVariant = Color(0xFF4A281D), onSurfaceVariant = Color(0xFFE8C3B1)
    )
    MusicTheme.LIGHT -> DarkColorScheme
}

@Composable
fun MusicPlayerTheme(
    theme: MusicTheme = MusicTheme.DEFAULT,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (theme == MusicTheme.LIGHT) LightColorScheme else if (darkTheme) darkColorSchemeFor(theme) else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
