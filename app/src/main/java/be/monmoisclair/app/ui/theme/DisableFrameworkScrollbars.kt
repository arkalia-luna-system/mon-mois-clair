package be.monmoisclair.app.ui.theme

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

/**
 * Coupe les thumbs de barre de défilement **framework** (View) sur la chaîne Compose.
 *
 * Sur Samsung One UI 7 + edge-to-edge, AndroidComposeView peut peindre un thumb gris
 * fixe (~120 dp) sur le bord **start** — hors design carnet, et trompeur (pas un scroll Compose).
 */
@Composable
fun DisableFrameworkScrollbars() {
    val view = LocalView.current
    SideEffect {
        var current: View? = view
        while (current != null) {
            current.isVerticalScrollBarEnabled = false
            current.isHorizontalScrollBarEnabled = false
            if (current is ViewGroup) {
                for (i in 0 until current.childCount) {
                    val child = current.getChildAt(i)
                    child.isVerticalScrollBarEnabled = false
                    child.isHorizontalScrollBarEnabled = false
                }
            }
            current = current.parent as? View
        }
    }
}
