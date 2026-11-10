package be.monmoisclair.app.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/** Évite deux copies de la même fiche sur la pile (double tap rapide). */
fun NavController.navigateSingleTop(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route) {
        launchSingleTop = true
        builder()
    }
}
