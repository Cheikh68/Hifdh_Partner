package com.example.hifdhpartner.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = LightButtonBackground, // Button color
    onPrimary = LightTextColor, // White text on button
    background = LightBackground, // Cyan blue screen background
    surface = LightBackground, // Same as background
    onSurface = LightTextColor // White text
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkButtonBackground, // Button color (black)
    onPrimary = DarkTextColor, // White text on button
    background = DarkBackground, // Dark blue screen background
    surface = DarkBackground, // Same as background
    onSurface = DarkTextColor // White text
)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)


@Composable
fun HifdhPartnerTheme(
    darkTheme: Boolean, // Controlled externally
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
