package br.com.saudefinanceira.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta: Deep Sage Green (base), Warm Ivory (fundo) e Vibrant Vermilion Red (destaque).
val SageGreen = Color(0xFF4B5A50)
val WarmIvory = Color(0xFFF1EEE3)
val VermilionRed = Color(0xFFF20B0B)

// Nomes antigos mantidos para as telas que ainda os importam (SpendingCard etc.).
val Teal = SageGreen
val Blue = VermilionRed

// Cabeçalhos e barras de progresso: verde-sálvia com leve profundidade.
val BrandGradient = Brush.linearGradient(colors = listOf(Color(0xFF5E7065), Color(0xFF3B4840)))

private val LightColors = lightColorScheme(
    primary = SageGreen,
    onPrimary = WarmIvory,
    primaryContainer = Color(0xFFDCE5DD),
    onPrimaryContainer = Color(0xFF1B241E),
    secondary = Color(0xFFD90A0A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD4),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = Color(0xFF8A6A1F),
    tertiaryContainer = Color(0xFFF6E3B4),
    onTertiaryContainer = Color(0xFF2E2200),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = WarmIvory,
    onBackground = Color(0xFF1C1F1B),
    surface = Color(0xFFFAF8F1),
    onSurface = Color(0xFF1C1F1B),
    surfaceVariant = Color(0xFFE3DFD0),
    onSurfaceVariant = Color(0xFF4A4D45),
    outline = Color(0xFF7B7F74),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB3C7B9),
    onPrimary = Color(0xFF1B241E),
    primaryContainer = Color(0xFF3A483F),
    onPrimaryContainer = Color(0xFFDCE5DD),
    secondary = Color(0xFFFF6B5C),
    onSecondary = Color(0xFF410002),
    secondaryContainer = Color(0xFF7A1410),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFFE2C26B),
    tertiaryContainer = Color(0xFF4A3A10),
    onTertiaryContainer = Color(0xFFFFE9C2),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF181D19),
    onBackground = Color(0xFFE6E3D8),
    surface = Color(0xFF212823),
    onSurface = Color(0xFFE6E3D8),
    surfaceVariant = Color(0xFF2E3830),
    onSurfaceVariant = Color(0xFFC3C9BE),
    outline = Color(0xFF8D9488),
)

@Composable
fun SaudeFinanceiraTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
