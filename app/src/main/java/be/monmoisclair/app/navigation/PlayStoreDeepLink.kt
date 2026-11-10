package be.monmoisclair.app.navigation

import android.content.Intent
import android.net.Uri

/**
 * Liens Play Store vers la fiche de l’app (Internal Testing / mise à jour).
 * Pas d’action réseau : ouverture = lancer l’activité normale.
 */
object PlayStoreDeepLink {

    private const val HOST = "play.google.com"
    private const val PATH_PREFIX = "/store/apps/details"

    fun isPlayStoreListingIntent(intent: Intent?): Boolean {
        val data: Uri = intent?.data ?: return false
        if (intent.action != Intent.ACTION_VIEW) return false
        if (data.scheme != "https" || data.host != HOST) return false
        return data.path.orEmpty().startsWith(PATH_PREFIX)
    }

    /** Lien Play vers une **autre** app — on ferme sans lire de données. */
    fun rejectsWrongPackage(intent: Intent?, packageName: String): Boolean {
        if (!isPlayStoreListingIntent(intent)) return false
        val id = intent?.data?.getQueryParameter("id")?.trim().orEmpty()
        return id.isNotEmpty() && id != packageName
    }
}
