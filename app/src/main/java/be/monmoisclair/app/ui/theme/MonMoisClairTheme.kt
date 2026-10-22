package be.monmoisclair.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.monmoisclair.app.R

private fun colorScheme(palette: CarnetPalette, night: Boolean) = run {
    if (night) {
        darkColorScheme(
            primary = palette.buttonFill,
            onPrimary = palette.buttonText,
            secondary = palette.accentOk,
            onSecondary = palette.buttonText,
            background = palette.paperCream,
            onBackground = palette.ink,
            surface = palette.paperWarm,
            onSurface = palette.ink,
            surfaceContainerHigh = palette.paperWarm,
            surfaceContainerHighest = palette.paperWarm,
            surfaceContainer = palette.paperCream,
            surfaceContainerLow = palette.paperCream,
            surfaceContainerLowest = palette.paperCream,
            error = palette.dangerInk,
            onError = palette.ink,
            outline = palette.fieldLine
        )
    } else {
        lightColorScheme(
            primary = palette.buttonFill,
            onPrimary = palette.buttonText,
            secondary = palette.accentOk,
            onSecondary = palette.buttonText,
            background = palette.paperCream,
            onBackground = palette.ink,
            surface = palette.paperWarm,
            onSurface = palette.ink,
            surfaceContainerHigh = palette.paperWarm,
            surfaceContainerHighest = palette.paperWarm,
            surfaceContainer = palette.paperCream,
            surfaceContainerLow = palette.paperCream,
            surfaceContainerLowest = palette.paperCream,
            error = palette.dangerInk,
            onError = palette.ink,
            outline = palette.fieldLine
        )
    }
}

/**
 * Trois rayons, pas un seul.
 * Boutons = [Shapes.small] 12 dp. Cartes = [Shapes.medium] 16 dp.
 * Pastilles = [StampShape] (capsule). Feuilles = [Shapes.extraLarge].
 */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Tampon (Accord, En retard, jour). Pas un bouton. */
val StampShape = RoundedCornerShape(50)

/** Source Serif 4 — chiffre, et le nom de l’app. OFL Adobe. */
internal val BrandSerif = FontFamily(
    Font(R.font.source_serif4_regular, FontWeight.Normal),
    Font(R.font.source_serif4_semibold, FontWeight.SemiBold),
    Font(R.font.source_serif4_semibold, FontWeight.Bold)
)

/** Source Sans 3 — corps / titres UI. OFL Adobe. */
private val UiSans = FontFamily(
    Font(R.font.source_sans3_regular, FontWeight.Normal),
    Font(R.font.source_sans3_semibold, FontWeight.Medium),
    Font(R.font.source_sans3_semibold, FontWeight.SemiBold),
    Font(R.font.source_sans3_semibold, FontWeight.Bold)
)

/** Chiffres de même chasse : 1 111,11 € et 9,00 € s’alignent. */
private const val TabularFigures = "tnum"

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = TabularFigures)

private fun appTypography(ink: Color, muted: Color) = Typography(
    displayLarge = TextStyle(
        fontFamily = BrandSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        color = ink
    ).tabular(),
    headlineMedium = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = ink
    ).tabular(),
    titleLarge = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minTitleSp.value.sp,
        lineHeight = 28.sp,
        color = ink
    ).tabular(),
    bodyLarge = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Normal,
        fontSize = A11yTokens.minBodySp,
        color = ink,
        lineHeight = 26.sp
    ).tabular(),
    bodyMedium = TextStyle(
        fontFamily = UiSans,
        fontSize = A11yTokens.minBodySp,
        color = muted,
        lineHeight = 24.sp
    ).tabular(),
    // Pas de couleur figée : les boutons Primary doivent hériter de
    // LocalContentColor (crème sur vert), pas d’Ink sombre illisible.
    labelLarge = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minBodySp
    ).tabular(),
    // Libellés flottants OutlinedTextField (≥ 18 sp, charte a11y).
    titleSmall = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = A11yTokens.minBodySp,
        color = ink
    ).tabular(),
    bodySmall = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Normal,
        fontSize = A11yTokens.minBodySp,
        color = muted,
        lineHeight = 24.sp
    ).tabular(),
    labelSmall = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Medium,
        fontSize = A11yTokens.minBodySp,
        color = muted
    ).tabular()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonMoisClairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val night = darkTheme
    val palette = if (night) A11yTokens.Night else A11yTokens.Day
    val ripple = RippleConfiguration(
        color = palette.buttonFill,
        rippleAlpha = RippleAlpha(
            draggedAlpha = 0.16f,
            focusedAlpha = 0.12f,
            hoveredAlpha = 0.08f,
            pressedAlpha = 0.16f
        )
    )
    CompositionLocalProvider(LocalCarnetPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme(palette, night),
            typography = appTypography(palette.ink, palette.inkMuted),
            shapes = AppShapes
        ) {
            CompositionLocalProvider(LocalRippleConfiguration provides ripple) {
                content()
            }
        }
    }
}
