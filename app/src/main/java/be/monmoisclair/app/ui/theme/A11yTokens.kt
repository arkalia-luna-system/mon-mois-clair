package be.monmoisclair.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Palette app-wide 7.11 — mist cool 2027 (plus de crème carnet).
 * Alignée [HomeTokens] ; contrastes ≥ 7:1 (tests WCAG).
 * Les getters Compose suivent [LocalCarnetPalette].
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
    /** Lien et indicateur. Jour = teal primary. Nuit = teal soft lisible. */
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

    /** Jour — mist cool + cartes blanches + teal profond (pas violet, pas crème). */
    val Day = CarnetPalette(
        paperCream = Color(0xFFF0F4F7),
        paperCard = Color(0xFFFFFFFF),
        paperWarm = Color(0xFFE4EEF2),
        paperBorder = Color(0xFFC9D5DE),
        fieldLine = Color(0xFF3D4F5C),
        ink = Color(0xFF121A24),
        inkMuted = Color(0xFF3D4F5C),
        accentOkSoft = Color(0xFFE3EFE6),
        accentOk = Color(0xFF1E5631),
        accentOkInk = Color(0xFF1E5631),
        accentWarn = Color(0xFF5C3D12),
        accentDanger = Color(0xFF8B2E18),
        dangerInk = Color(0xFF8B2E18),
        accentAuto = Color(0xFFD99B26),
        onGold = Color(0xFF121A24),
        buttonFill = Color(0xFF0A5254),
        buttonText = Color(0xFFFFFFFF),
        link = Color(0xFF0A5254)
    )

    /**
     * Nuit — surfaces ardoise ; fills FAIT/retard inchangés.
     * Lien soft ≥ 7:1 sur mid surface.
     */
    val Night = CarnetPalette(
        paperCream = Color(0xFF12181E),
        paperCard = Color(0xFF1C242C),
        paperWarm = Color(0xFF2A343C),
        paperBorder = Color(0xFF3A4A56),
        fieldLine = Color(0xFFB7C4CE),
        ink = Color(0xFFF0F4F7),
        inkMuted = Color(0xFFB7C4CE),
        accentOkSoft = Color(0xFF1C3326),
        accentOk = Color(0xFF1E5631),
        accentOkInk = Color(0xFF9AD9B6),
        accentWarn = Color(0xFFE2C08A),
        accentDanger = Color(0xFF8E3418),
        dangerInk = Color(0xFFF4C4B6),
        accentAuto = Color(0xFFD99B26),
        onGold = Color(0xFF121A24),
        buttonFill = Color(0xFF0A5254),
        buttonText = Color(0xFFFFFFFF),
        link = Color(0xFF8FD4D0)
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
