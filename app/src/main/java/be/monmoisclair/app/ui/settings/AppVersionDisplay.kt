package be.monmoisclair.app.ui.settings

/**
 * Affichage famille de [versionName] Gradle (ex. `0.1.0-6.23`).
 * L’écran montre le nom produit + l’étape (`6.23`), jamais le préfixe `0.1.0-`.
 */
object AppVersionDisplay {
    fun stepLabel(versionName: String?): String? {
        if (versionName.isNullOrBlank()) return null
        val step = versionName.substringAfterLast('-')
        if (step == versionName || step.isBlank()) return null
        return step
    }
}
