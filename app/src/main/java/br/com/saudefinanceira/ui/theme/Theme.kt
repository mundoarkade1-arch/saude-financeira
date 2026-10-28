package br.com.saudefinanceira.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Verde-azulado (calma, crescimento) -> azul (confiança, estabilidade).
val Teal = Color(0xFF0FA39A)
val Blue = Color(0xFF2F6FDB)

val BrandGradient = Brush.linearGradient(
    colors = listOf(Teal, Blue),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1000f),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B8F87),
    onPrimary = Color.White,
    secondary = Blue,
    background = Color(0xFFF4F8FA),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FD1C5),
    onPrimary = Color(0xFF00201E),
    secondary = Color(0xFF7AA7F5),
    background = Color(0xFF0E1A1F),
    surface = Color(0xFF162329),
)

@Composable
fun SaudeFinanceiraTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
