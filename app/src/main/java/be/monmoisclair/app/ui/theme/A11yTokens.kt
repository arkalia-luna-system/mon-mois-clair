package be.monmoisclair.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object A11yTokens {
    val minBodySp = 18.sp
    val minTitleSp = 22.sp
    val minTouchHeight = 48.dp
    val screenPadding = 24.dp
    /**
     * Bande vide **obligatoire** au-dessus des boutons tactiles système (3 boutons / geste).
     * Appliquée **une seule fois** dans `MainActivity` (avec safeDrawing + tappableElement).
     * Ne pas la re-ajouter dans les LazyColumn / écrans — sinon double marge sur S26.
     */
    val bottomSystemClearance = 28.dp

    val PaperCream = Color(0xFFF5F0E6)
    val PaperWarm = Color(0xFFEDE6D9)
    val Ink = Color(0xFF1A1A1A)
    val InkMuted = Color(0xFF4A453C)
    val AccentOk = Color(0xFF2F6B4F)
    val AccentWarn = Color(0xFF8A5A1E)
    val AccentDanger = Color(0xFF8B2E2E)
    val ButtonFill = Color(0xFF2C4A3E)
    val ButtonText = Color(0xFFF5F0E6)
}
