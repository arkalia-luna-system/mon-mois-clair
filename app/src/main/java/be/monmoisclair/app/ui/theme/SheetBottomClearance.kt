package be.monmoisclair.app.ui.theme

import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Marge basse pour une [androidx.compose.material3.ModalBottomSheet] (fenêtre dialogue).
 *
 * Sur Galaxy + edge-to-edge, Compose peut rapporter **0** inset dans la feuille alors que
 * la barre de navigation fait ~48 dp — les puces mois se retrouvent coupées.
 * On croise Compose + framework, avec plancher 48 dp + [A11yTokens.bottomSystemClearance].
 */
object SheetBottomClearance {
    /** Plancher barre 3 boutons / geste (dp). */
    val MinNavBar = 48.dp

    fun resolve(composeInset: Dp, frameworkInset: Dp): Dp =
        max(max(composeInset, frameworkInset), MinNavBar) + A11yTokens.bottomSystemClearance
}

@Composable
fun rememberSheetBottomClearance(): Dp {
    val density = LocalDensity.current
    val view = LocalView.current
    val composePx = WindowInsets.navigationBars
        .union(WindowInsets.tappableElement)
        .getBottom(density)
    val frameworkPx = frameworkSystemBottomPx(view)
    return SheetBottomClearance.resolve(
        composeInset = with(density) { composePx.toDp() },
        frameworkInset = with(density) { frameworkPx.toDp() }
    )
}

private fun frameworkSystemBottomPx(view: View): Int {
    val insets = ViewCompat.getRootWindowInsets(view) ?: return 0
    val types = WindowInsetsCompat.Type.navigationBars() or
        WindowInsetsCompat.Type.tappableElement()
    return insets.getInsets(types).bottom
}
