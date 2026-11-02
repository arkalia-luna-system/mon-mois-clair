package be.monmoisclair.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Ajustement d’affichage selon la **fenêtre** (pas le modèle Samsung).
 * Priorité usage principal = écran « normal » (~S26 6,3″) : header Home plus compact.
 *
 * **Zone boutons système (obligatoire)** : gérée une seule fois dans `MainActivity`
 * (`safeDrawing` + `tappableElement` + [A11yTokens.bottomSystemClearance]) —
 * les listes ne doivent **pas** rajouter cette bande (sinon double marge).
 *
 * Sources : Samsung.com (S25 Ultra 6,9″ QHD+ / S26 6,3″ FHD+) ·
 * One UI Design Guide (marges ≥ 24 dp) · Android Window size classes (phones = Compact < 600 dp).
 */
@Immutable
data class DisplayFit(
    val screenHeightDp: Int,
    val screenWidthDp: Int,
    /** Mode sticky Home : plus serré sur écrans courts (S26 / affichage agrandi). */
    val compactHomeChrome: Boolean,
    val screenPadding: Dp,
    val stickyPaddingVertical: Dp,
    val stickyPaddingHorizontal: Dp,
    val bannerPadding: Dp,
    val sectionGap: Dp,
    /**
     * Un seul chiffre. Charte étude ~36–42 confort / ≥ 34 compact —
     * 7.07 : sticky Accueil toujours densifié (liste > air Ultra).
     */
    val remainingAmountSp: Float
) {
    /** Padding bannières / tips d’état. */
    val tipPad: Dp get() = if (compactHomeChrome) 10.dp else 12.dp

    /** Padding tips un peu plus aérés (onboarding / scan). */
    val tipPadLg: Dp get() = if (compactHomeChrome) 12.dp else 14.dp

    companion object {
        /**
         * Sous ~800 dp de hauteur, le sticky mange trop la liste
         * (S26 FHD+ ≈ 780–832 dp ; densités élevées One UI aussi).
         * Autres surfaces (liste pad, tips) : compact vs confort.
         * Sticky Accueil : densités agressives **sur toutes hauteurs** (7.07 pack B).
         */
        const val COMPACT_HEIGHT_DP = 800

        fun from(screenWidthDp: Int, screenHeightDp: Int): DisplayFit {
            val compact = screenHeightDp <= COMPACT_HEIGHT_DP
            return DisplayFit(
                screenHeightDp = screenHeightDp,
                screenWidthDp = screenWidthDp,
                compactHomeChrome = compact,
                screenPadding = A11yTokens.screenPadding,
                // 7.07 pack B : même densités sticky Ultra / S26 — liste d’abord.
                stickyPaddingVertical = 6.dp,
                stickyPaddingHorizontal = A11yTokens.screenPadding,
                bannerPadding = if (compact) 8.dp else 12.dp,
                sectionGap = 2.dp,
                remainingAmountSp = 34f
            )
        }
    }
}

/**
 * Pré-compensation hero quand la police système est agrandie (P1-3 / a11y).
 * Testable hors Compose.
 */
fun DisplayFit.withFontScale(fontScale: Float): DisplayFit {
    if (fontScale < 1.25f) return this
    val visualCap = when {
        fontScale >= 1.8f -> 28f
        fontScale >= 1.5f -> 30f
        else -> min(remainingAmountSp, 34f)
    }
    return copy(
        stickyPaddingVertical = minDp(stickyPaddingVertical, 6.dp),
        bannerPadding = minDp(bannerPadding, 8.dp),
        sectionGap = minDp(sectionGap, 2.dp),
        remainingAmountSp = visualCap / fontScale
    )
}

@Composable
fun rememberDisplayFit(): DisplayFit {
    val cfg = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val base = DisplayFit.from(
        screenWidthDp = cfg.screenWidthDp,
        screenHeightDp = cfg.screenHeightDp
    )
    return base.withFontScale(fontScale)
}

private fun minDp(a: Dp, b: Dp): Dp = if (a < b) a else b
