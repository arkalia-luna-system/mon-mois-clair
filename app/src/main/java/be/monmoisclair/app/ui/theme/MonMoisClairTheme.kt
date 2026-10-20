package be.monmoisclair.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorScheme = lightColorScheme(
    primary = A11yTokens.ButtonFill,
    onPrimary = A11yTokens.ButtonText,
    secondary = A11yTokens.AccentOk,
    onSecondary = A11yTokens.ButtonText,
    background = A11yTokens.PaperCream,
    onBackground = A11yTokens.Ink,
    surface = A11yTokens.PaperWarm,
    onSurface = A11yTokens.Ink,
    // AlertDialog / sheets Material3 (évite le lavande par défaut)
    surfaceContainerHigh = A11yTokens.PaperWarm,
    surfaceContainerHighest = A11yTokens.PaperWarm,
    surfaceContainer = A11yTokens.PaperCream,
    surfaceContainerLow = A11yTokens.PaperCream,
    surfaceContainerLowest = A11yTokens.PaperCream,
    error = A11yTokens.AccentDanger,
    onError = A11yTokens.ButtonText,
    outline = A11yTokens.FieldLine
)

/** Champs, dialogues et sheets : 12 dp partout (One UI / lisibilité). */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        color = A11yTokens.Ink
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = A11yTokens.Ink
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minTitleSp.value.sp,
        lineHeight = 28.sp,
        color = A11yTokens.Ink
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = A11yTokens.minBodySp,
        color = A11yTokens.Ink,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = A11yTokens.minBodySp,
        color = A11yTokens.InkMuted,
        lineHeight = 24.sp
    ),
    // Pas de couleur figée : les boutons Primary doivent hériter de
    // LocalContentColor (crème sur vert), pas d’Ink sombre illisible.
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minBodySp
    ),
    // Libellés flottants OutlinedTextField (≥ 18 sp, charte a11y).
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minBodySp,
        color = A11yTokens.Ink
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = A11yTokens.minBodySp,
        color = A11yTokens.InkMuted,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = A11yTokens.minBodySp,
        color = A11yTokens.InkMuted
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonMoisClairTheme(content: @Composable () -> Unit) {
    val ripple = RippleConfiguration(
        color = A11yTokens.ButtonFill,
        rippleAlpha = RippleAlpha(
            draggedAlpha = 0.16f,
            focusedAlpha = 0.12f,
            hoveredAlpha = 0.08f,
            pressedAlpha = 0.16f
        )
    )
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = AppTypography,
        shapes = AppShapes
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides ripple) {
            content()
        }
    }
}
