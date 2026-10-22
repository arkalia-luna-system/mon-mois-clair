package be.monmoisclair.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ajustement d’affichage selon la **fenêtre** (pas le modèle Samsung).
 * Priorité maman = écran « normal » (~S26 6,3″) : header Home plus compact.
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
    /** Un seul chiffre. Assez grand pour être le signal, assez court pour laisser la liste. */
    val remainingAmountSp: Float
) {
    companion object {
        /**
         * Sous ~800 dp de hauteur, le sticky Ultra mange trop la liste
         * (S26 FHD+ ≈ 780–832 dp ; Ultra @ densité élevée One UI aussi).
         */
        const val COMPACT_HEIGHT_DP = 800

        fun from(screenWidthDp: Int, screenHeightDp: Int): DisplayFit {
            val compact = screenHeightDp <= COMPACT_HEIGHT_DP
            return DisplayFit(
                screenHeightDp = screenHeightDp,
                screenWidthDp = screenWidthDp,
                compactHomeChrome = compact,
                screenPadding = A11yTokens.screenPadding,
                // Sticky léger : le montant « Reste » domine ; la marque est une légende.
                // Court (~S26) : encore un cran pour laisser la liste + FAIT lisibles.
                // H11 : un peu plus d’air sous le chiffre, sans re-gonfler le sticky Ultra.
                stickyPaddingVertical = if (compact) 8.dp else 14.dp,
                stickyPaddingHorizontal = A11yTokens.screenPadding,
                bannerPadding = if (compact) 8.dp else 12.dp,
                sectionGap = if (compact) 4.dp else 6.dp,
                remainingAmountSp = if (compact) 32f else 36f
            )
        }
    }
}

@Composable
fun rememberDisplayFit(): DisplayFit {
    val cfg = LocalConfiguration.current
    return DisplayFit.from(
        screenWidthDp = cfg.screenWidthDp,
        screenHeightDp = cfg.screenHeightDp
    )
}
