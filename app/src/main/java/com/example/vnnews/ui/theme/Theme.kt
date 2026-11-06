package com.example.vnnews.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF91D6C3),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF174F42),
    onPrimaryContainer = Color(0xFFB0EEDB),
    secondary = Color(0xFFB5CCC3),
    secondaryContainer = Color(0xFF304C40),
    onSecondaryContainer = Color(0xFFD6EDDF),
    tertiary = Color(0xFFE9A589),
    background = Color(0xFF141916),
    surface = Color(0xFF202822),
    surfaceContainerLow = Color(0xFF1B231E),
    surfaceVariant = Color(0xFF26332D),
    onBackground = Color(0xFFE2E9E4),
    onSurface = Color(0xFFE2E9E4),
    onSurfaceVariant = Color(0xFFB8C6BD),
    outlineVariant = Color(0xFF3C4942)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF255D4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFE6),
    onPrimaryContainer = Color(0xFF174F3D),
    secondary = Color(0xFF50665B),
    secondaryContainer = Color(0xFFDCECE2),
    onSecondaryContainer = Color(0xFF244C3A),
    tertiary = Color(0xFFA65336),
    background = Color(0xFFF8F7F3),
    surface = Color(0xFFFFFEFB),
    surfaceContainerLow = Color(0xFFF0F0E9),
    surfaceVariant = Color(0xFFE7EDE5),
    onBackground = Color(0xFF1C2922),
    onSurface = Color(0xFF1C2922),
    onSurfaceVariant = Color(0xFF535F55),
    outlineVariant = Color(0xFFE0E3D9)
)

@Composable
fun VnNewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
