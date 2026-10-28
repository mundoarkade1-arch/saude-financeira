package br.com.saudefinanceira.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Verde-azulado (calma, crescimento) -> azul (confiança, estabilidade).
val Teal = Color(0xFF0FA39A)
val Blue = Color(0xFF2F6FDB)

val BrandGradient = Brush.linearGradient(colors = listOf(Teal, Blue))

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B8F87),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2F4F0),
    onPrimaryContainer = Color(0xFF00201E),
    secondary = Blue,
    secondaryContainer = Color(0xFFD7E5FF),
    onSecondaryContainer = Color(0xFF001B3F),
    tertiaryContainer = Color(0xFFFFE9C2),
    onTertiaryContainer = Color(0xFF3A2A00),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF4F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE3ECEF),
    onSurfaceVariant = Color(0xFF3F4A4F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FD1C5),
    onPrimary = Color(0xFF00201E),
    primaryContainer = Color(0xFF0F4F4A),
    onPrimaryContainer = Color(0xFFBDF4EE),
    secondary = Color(0xFF7AA7F5),
    secondaryContainer = Color(0xFF1F3E6B),
    onSecondaryContainer = Color(0xFFD7E5FF),
    tertiaryContainer = Color(0xFF4A3A10),
    onTertiaryContainer = Color(0xFFFFE9C2),
    background = Color(0xFF0E1A1F),
    surface = Color(0xFF162329),
    surfaceVariant = Color(0xFF24343B),
    onSurfaceVariant = Color(0xFFB9C7CC),
)

@Composable
fun SaudeFinanceiraTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
