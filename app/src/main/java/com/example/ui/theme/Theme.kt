package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AmberTerracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = AmberTerracottaDark,
    secondary = ForestSage,
    onSecondary = Color.White,
    secondaryContainer = SageLight,
    onSecondaryContainer = DeepCharcoal,
    background = WarmIvory,
    onBackground = DeepCharcoal,
    surface = Color.White,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftCream,
    onSurfaceVariant = DeepCharcoal,
    outline = BorderSubtle
)

@Composable
fun HappyPawsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
