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
    primary = AmberTerracotta,
    onPrimary = Color.White,
    secondary = ForestSage,
    onSecondary = Color.White,
    tertiary = WarmAmber,
    background = Color(0xFF131517),
    surface = Color(0xFF1D2125),
    surfaceVariant = Color(0xFF282D33),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF3F4650)
)

private val LightColorScheme = lightColorScheme(
    primary = AmberTerracotta,
    onPrimary = Color.White,
    primaryContainer = SoftSage,
    onPrimaryContainer = ForestSage,
    secondary = ForestSage,
    onSecondary = Color.White,
    secondaryContainer = SoftCream,
    onSecondaryContainer = DeepCharcoal,
    tertiary = WarmAmber,
    onTertiary = Color.White,
    background = WarmIvory,
    onBackground = DeepCharcoal,
    surface = CardWarmSurface,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftCream,
    onSurfaceVariant = DeepCharcoal,
    outline = BorderSubtle,
    outlineVariant = WarmSand
)

@Composable
fun HappyPawsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep false by default to preserve the signature warm ivory & terracotta Happy Paws brand
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
