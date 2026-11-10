package be.monmoisclair.app.ui.home

import android.content.Context

/**
 * Nouveautés à l’ouverture — contenu = [WhatsNewCatalog] ; logique de sélection ici.
 */
object WhatsNewCopy {
    const val TITLE = "Nouveautés"
    const val DISMISS = "J’ai compris"
    const val MAX_BULLETS = 6

    data class Payload(
        val bullets: List<String>,
        /** Ex. « Version 7.42 » — affiché sous le titre. */
        val versionLabel: String?,
    )

    fun versionLabelFromName(versionName: String?): String? {
        val step = versionName?.substringAfterLast('-', missingDelimiterValue = "")?.trim().orEmpty()
        return step.takeIf { it.isNotEmpty() }?.let { "Version $it" }
    }

    /**
     * @param lastSeenVersionCode 0 = jamais vu (première ouverture ou ancienne install).
     * @param currentVersionCode version installée.
     * @param installedVersionName `PackageManager` versionName (ex. 0.1.0-7.42).
     */
    fun payload(
        context: Context,
        lastSeenVersionCode: Int,
        currentVersionCode: Int,
        installedVersionName: String? = null,
    ): Payload? {
        if (currentVersionCode <= 0) return null
        if (lastSeenVersionCode >= currentVersionCode) return null
        val catalog = WhatsNewCatalog.load(context)
        val release = catalog.releases.lastOrNull { it.versionCode == currentVersionCode }
        if (release != null && !release.userVisible) return null
        val bullets = release
            ?.bullets
            .orEmpty()
            .distinct()
            .take(MAX_BULLETS)
        val resolved = bullets.ifEmpty {
            listOf(catalog.genericFallbackBullet)
        }
        return Payload(
            bullets = resolved,
            versionLabel = versionLabelFromName(installedVersionName),
        )
    }

    /** Tests JVM sans Context : releases + fallback injectés. */
    fun payloadForReleases(
        lastSeenVersionCode: Int,
        currentVersionCode: Int,
        releases: List<WhatsNewCatalog.Release>,
        genericFallbackBullet: String,
        installedVersionName: String? = null,
    ): Payload? {
        if (currentVersionCode <= 0) return null
        if (lastSeenVersionCode >= currentVersionCode) return null
        val release = releases.lastOrNull { it.versionCode == currentVersionCode }
        if (release != null && !release.userVisible) return null
        val bullets = release
            ?.bullets
            .orEmpty()
            .distinct()
            .take(MAX_BULLETS)
        val resolved = bullets.ifEmpty {
            listOf(genericFallbackBullet)
        }
        return Payload(
            bullets = resolved,
            versionLabel = versionLabelFromName(installedVersionName),
        )
    }

    const val POST_WHATS_NEW_HELP_TIP =
        "Astuce : Réglages → Comment ça marche pour relire calmement plus tard."

    const val BACKUP_NUDGE_TITLE = "Pensez à une copie"
    const val BACKUP_NUDGE_BODY =
        "Vous n’avez pas encore fait de copie. Réglages → Sauvegarde : " +
            "1) noter le code sur papier · 2) choisir le dossier · 3) envoyer une copie."
    const val BACKUP_NUDGE_LATER = "Plus tard"
    const val BACKUP_NUDGE_OPEN = "Ouvrir Sauvegarde"
}
