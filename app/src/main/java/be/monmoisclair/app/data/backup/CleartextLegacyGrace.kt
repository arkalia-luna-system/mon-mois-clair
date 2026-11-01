package be.monmoisclair.app.data.backup

/**
 * Grâce restore des copies ZIP/JSON claires pré-6.81.
 *
 * **6.99 :** butoir produit **fermé** — l’opt-in UI disparaît et
 * [BackupRestorer] ignore toute demande `allowCleartextLegacy`.
 * Les copies protégées `.mmc` / `MMC1B64` restent le seul chemin.
 */
object CleartextLegacyGrace {

    /** false = deadline atteinte ; true = filet rare encore affiché. */
    const val OPT_IN_AVAILABLE = false

    /** Garde unique : jamais de clair si la grâce est close. */
    fun effectiveAllow(requested: Boolean): Boolean =
        OPT_IN_AVAILABLE && requested
}
