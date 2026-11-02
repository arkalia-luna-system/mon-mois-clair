package be.monmoisclair.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system Accueil + référence visuelle 7.13 — mist cool 2027.
 * Palette alignée [A11yTokens] (thème global). Touch ≥ 48 · contrastes ≥ 7:1.
 */
@Immutable
data class HomePalette(
    val canvas: Color,
    val heroSurface: Color,
    val card: Color,
    val cardBorder: Color,
    val chip: Color,
    val coursesChip: Color,
    val savingsChip: Color,
    val ink: Color,
    val inkMuted: Color,
    val link: Color,
    /** Texte déficit / retard sur canvas. */
    val dangerInk: Color,
    /** Fond pastille retard / FAIT danger. */
    val dangerFill: Color,
    /** Texte « c’est bon » sur canvas. */
    val okInk: Color,
    /** Fond pastille / bouton FAIT. */
    val okFill: Color,
    val okSoft: Color,
    val warn: Color,
    val barSurface: Color,
    val divider: Color
)

object HomeTokens {
    val minTouch = 48.dp
    val heroRadius = RoundedCornerShape(28.dp)
    val cardRadius = RoundedCornerShape(20.dp)
    val chipRadius = RoundedCornerShape(16.dp)
    val wellRadius = RoundedCornerShape(18.dp)
    val buttonRadius = RoundedCornerShape(16.dp)
    val cardGap = 14.dp
    val screenPad = 20.dp
    /** Tonal > carte bancaire : ombre dial-down. */
    val heroElevation = 1.5.dp
    val cardElevation = 2.dp
    val barElevation = 4.dp
    val hairline = 0.5.dp
    /** Empty Accueil / bandeau statut. */
    val emptyPadVertical = 32.dp

    /**
     * Jour — mist cool + hero mid tonal + teal profond (pas violet).
     * Contrastes ink/muted/link/danger/ok/warn ≥ 7:1 sur canvas et card (tests).
     */
    val Day = HomePalette(
        canvas = Color(0xFFF0F4F7),
        heroSurface = Color(0xFFE8F1F4),
        card = Color(0xFFFFFFFF),
        cardBorder = Color(0xFFC9D5DE),
        chip = Color(0xFFE4EEF2),
        coursesChip = Color(0xFFD9EAF2),
        savingsChip = Color(0xFFDCEDE3),
        ink = Color(0xFF121A24),
        inkMuted = Color(0xFF3D4F5C),
        link = Color(0xFF0A5254),
        dangerInk = Color(0xFF8B2E18),
        dangerFill = Color(0xFF8B2E18),
        okInk = Color(0xFF1E5631),
        okFill = Color(0xFF1E5631),
        okSoft = Color(0xFFE3EFE6),
        warn = Color(0xFF5C3D12),
        barSurface = Color(0xFFFFFFFF),
        divider = Color(0xFFC9D5DE)
    )

    /** Nuit — surfaces ardoise ; fills sombres pour pastilles (texte crème). */
    val Night = HomePalette(
        canvas = Color(0xFF12181E),
        heroSurface = Color(0xFF243038),
        card = Color(0xFF1C242C),
        cardBorder = Color(0xFF3A4A56),
        chip = Color(0xFF2A343C),
        coursesChip = Color(0xFF243440),
        savingsChip = Color(0xFF24362C),
        ink = Color(0xFFF0F4F7),
        inkMuted = Color(0xFFB7C4CE),
        link = Color(0xFF8FD4D0),
        dangerInk = Color(0xFFF4C4B6),
        dangerFill = Color(0xFF8E3418),
        okInk = Color(0xFF9AD9B6),
        okFill = Color(0xFF1E5631),
        okSoft = Color(0xFF1C3326),
        warn = Color(0xFFE2C08A),
        barSurface = Color(0xFF1C242C),
        divider = Color(0xFF3A4A56)
    )

    private val palette: HomePalette
        @Composable @ReadOnlyComposable
        get() = if (LocalCarnetPalette.current === A11yTokens.Night) Night else Day

    val Canvas: Color @Composable @ReadOnlyComposable get() = palette.canvas
    val HeroSurface: Color @Composable @ReadOnlyComposable get() = palette.heroSurface
    val Card: Color @Composable @ReadOnlyComposable get() = palette.card
    val CardBorder: Color @Composable @ReadOnlyComposable get() = palette.cardBorder
    val Chip: Color @Composable @ReadOnlyComposable get() = palette.chip
    val CoursesChip: Color @Composable @ReadOnlyComposable get() = palette.coursesChip
    val SavingsChip: Color @Composable @ReadOnlyComposable get() = palette.savingsChip
    val Ink: Color @Composable @ReadOnlyComposable get() = palette.ink
    val InkMuted: Color @Composable @ReadOnlyComposable get() = palette.inkMuted
    val Link: Color @Composable @ReadOnlyComposable get() = palette.link
    val Danger: Color @Composable @ReadOnlyComposable get() = palette.dangerInk
    val DangerFill: Color @Composable @ReadOnlyComposable get() = palette.dangerFill
    val Ok: Color @Composable @ReadOnlyComposable get() = palette.okInk
    val OkFill: Color @Composable @ReadOnlyComposable get() = palette.okFill
    val OkSoft: Color @Composable @ReadOnlyComposable get() = palette.okSoft
    val Warn: Color @Composable @ReadOnlyComposable get() = palette.warn
    val BarSurface: Color @Composable @ReadOnlyComposable get() = palette.barSurface
    val Divider: Color @Composable @ReadOnlyComposable get() = palette.divider
}
