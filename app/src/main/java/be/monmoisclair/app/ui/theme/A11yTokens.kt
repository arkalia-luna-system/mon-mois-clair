package be.monmoisclair.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Couleurs du carnet. Le jour est le papier. La nuit est la même encre,
 * pas la couleur du fond d’écran et pas un thème fluo.
 * Les champs se lisent dans un composable : ils suivent [LocalCarnetPalette].
 * Les tests lisent [A11yTokens.Day] et [A11yTokens.Night].
 */
@Immutable
data class CarnetPalette(
    val paperCream: Color,
    val paperCard: Color,
    val paperWarm: Color,
    val paperBorder: Color,
    val fieldLine: Color,
    val ink: Color,
    val inkMuted: Color,
    val accentOkSoft: Color,
    /** Plein FAIT / pastille. Reste forêt, jour et nuit. */
    val accentOk: Color,
    /** Texte « c’est bon » sur le fond. */
    val accentOkInk: Color,
    val accentWarn: Color,
    /** Plein retard. Reste brique, jour et nuit. */
    val accentDanger: Color,
    /** Texte retard / déficit sur le fond. */
    val dangerInk: Color,
    val accentAuto: Color,
    /** Mot sur la pastille or. Toujours l’encre, jamais le texte de page. */
    val onGold: Color,
    val buttonFill: Color,
    val buttonText: Color,
    /** Lien et indicateur. Le jour = le teal du bouton. La nuit, un teal lisible sur l’encre. */
    val link: Color
)

val LocalCarnetPalette = staticCompositionLocalOf { A11yTokens.Day }

object A11yTokens {
    val minBodySp = 18.sp
    val minTitleSp = 22.sp
    val minTouchHeight = 48.dp
    val screenPadding = 24.dp
    /** Espace min entre cartes d’échéances (étude One UI / faux clics). */
    val cardGap = 12.dp
    /**
     * Bande vide **obligatoire** au-dessus des boutons tactiles système (3 boutons / geste).
     * Appliquée **une seule fois** dans `MainActivity` (avec safeDrawing + tappableElement).
     * Ne pas la re-ajouter dans les LazyColumn / écrans — sinon double marge sur S26.
     */
    val bottomSystemClearance = 28.dp

    /** Papier du jour. Brique sur le fond ≥ 7:1. */
    val Day = CarnetPalette(
        paperCream = Color(0xFFF6F3E8),
        paperCard = Color(0xFFFFFDF9),
        paperWarm = Color(0xFFEDE8DC),
        paperBorder = Color(0xFFE2DDD2),
        fieldLine = Color(0xFF8A8278),
        ink = Color(0xFF1E1B18),
        inkMuted = Color(0xFF4A453C),
        accentOkSoft = Color(0xFFE3EFE6),
        accentOk = Color(0xFF1E5631),
        accentOkInk = Color(0xFF1E5631),
        // Brun warn / déficit ≥ 7:1 sur crème **et** PaperWarm sticky (#EDE8DC).
        accentWarn = Color(0xFF5A3A12),
        accentDanger = Color(0xFF7A2C14),
        dangerInk = Color(0xFF7A2C14),
        accentAuto = Color(0xFFD99B26),
        onGold = Color(0xFF1E1B18),
        buttonFill = Color(0xFF1B4D4E),
        buttonText = Color(0xFFFFFDF9),
        link = Color(0xFF1B4D4E)
    )

    /**
     * Encre de nuit. Le bouton reste le teal `#1B4D4E`.
     * Le texte « retard » et le lien sont éclaircis : la brique du jour
     * disparaîtrait sur ce fond.
     */
    val Night = CarnetPalette(
        paperCream = Color(0xFF1E1B18),
        paperCard = Color(0xFF2C2824),
        paperWarm = Color(0xFF3A342E),
        paperBorder = Color(0xFF6E6458),
        fieldLine = Color(0xFFC4B8A8),
        ink = Color(0xFFF6F3E8),
        // Un cran plus mat que l’encre (écart PageTitle / corps) ; warm reste ≥ 7:1.
        inkMuted = Color(0xFFD1C1B1),
        accentOkSoft = Color(0xFF1C3326),
        accentOk = Color(0xFF1E5631),
        accentOkInk = Color(0xFF9AD9B6),
        accentWarn = Color(0xFFE2C08A),
        accentDanger = Color(0xFF8E3418),
        dangerInk = Color(0xFFF4C4B6),
        accentAuto = Color(0xFFD99B26),
        onGold = Color(0xFF1E1B18),
        buttonFill = Color(0xFF1B4D4E),
        buttonText = Color(0xFFFFFDF9),
        link = Color(0xFF9AD8D8)
    )

    private val palette: CarnetPalette
        @Composable @ReadOnlyComposable get() = LocalCarnetPalette.current

    val PaperCream: Color @Composable @ReadOnlyComposable get() = palette.paperCream
    val PaperCard: Color @Composable @ReadOnlyComposable get() = palette.paperCard
    val PaperWarm: Color @Composable @ReadOnlyComposable get() = palette.paperWarm
    val PaperBorder: Color @Composable @ReadOnlyComposable get() = palette.paperBorder
    val FieldLine: Color @Composable @ReadOnlyComposable get() = palette.fieldLine
    val Ink: Color @Composable @ReadOnlyComposable get() = palette.ink
    val InkMuted: Color @Composable @ReadOnlyComposable get() = palette.inkMuted
    val AccentOkSoft: Color @Composable @ReadOnlyComposable get() = palette.accentOkSoft
    val AccentOk: Color @Composable @ReadOnlyComposable get() = palette.accentOk
    val AccentOkInk: Color @Composable @ReadOnlyComposable get() = palette.accentOkInk
    val AccentWarn: Color @Composable @ReadOnlyComposable get() = palette.accentWarn
    val AccentDanger: Color @Composable @ReadOnlyComposable get() = palette.accentDanger
    val DangerInk: Color @Composable @ReadOnlyComposable get() = palette.dangerInk
    val AccentAuto: Color @Composable @ReadOnlyComposable get() = palette.accentAuto
    val OnGold: Color @Composable @ReadOnlyComposable get() = palette.onGold
    val ButtonFill: Color @Composable @ReadOnlyComposable get() = palette.buttonFill
    val ButtonText: Color @Composable @ReadOnlyComposable get() = palette.buttonText
    val Link: Color @Composable @ReadOnlyComposable get() = palette.link
}
