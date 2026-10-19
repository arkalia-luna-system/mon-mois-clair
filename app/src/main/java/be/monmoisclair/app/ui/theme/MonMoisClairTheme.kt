package be.monmoisclair.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    error = A11yTokens.AccentDanger,
    onError = A11yTokens.ButtonText
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        color = A11yTokens.Ink
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        color = A11yTokens.Ink
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minTitleSp.value.sp,
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
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minBodySp,
        color = A11yTokens.Ink
    )
)

@Composable
fun MonMoisClairTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = AppTypography,
        content = content
    )
}
