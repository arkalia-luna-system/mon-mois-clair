package be.monmoisclair.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    /** Wax Paper — fond principal (AMOLED sans halo blanc pur). */
    val PaperCream = Color(0xFFF7F4EB)
    /** Warm Ivory — cartes factures. */
    val PaperCard = Color(0xFFFFFDF9)
    /** Ancien PaperWarm → surface sticky / champs (ivoire un peu plus marqué). */
    val PaperWarm = Color(0xFFEDE8DC)
    val PaperBorder = Color(0xFFE2DDD2)
    /** Trait des champs : visible, plus doux que l’encre des boutons. */
    val FieldLine = Color(0xFF6F675C)
    /** Deep Espresso. */
    val Ink = Color(0xFF1E1B18)
    val InkMuted = Color(0xFF4A453C)
    /** Soft Sage / forêt — FAIT. */
    val AccentOkSoft = Color(0xFFE3EFE6)
    val AccentOk = Color(0xFF1E5631)
    val AccentWarn = Color(0xFF8A5A1E)
    /**
     * Brique automne — retard / erreur.
     * #B84A27 de l’étude ne fait que ~4,7:1 sur le papier : trop juste.
     * #8E3418 reste terre cuite (pas rouge sang) et passe ~7:1.
     */
    val AccentDanger = Color(0xFF8E3418)
    /**
     * Or doux — **fond** de pastille « Auto » seulement.
     * En texte sur le papier il tombe à ~2,2:1. Le mot « Auto » s’écrit en [Ink].
     */
    val AccentAuto = Color(0xFFD99B26)
    /** Transformative Teal — boutons / stabilité. */
    val ButtonFill = Color(0xFF1B4D4E)
    val ButtonText = Color(0xFFFFFDF9)
}
