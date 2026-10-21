package be.monmoisclair.app.ui.scan

/**
 * Passage QR → fiche **en mémoire seulement**.
 * Évite de laisser l’IBAN / la référence dans [androidx.lifecycle.SavedStateHandle]
 * (persistance / process death).
 */
object EpcScanHandoff {
    @Volatile
    private var pendingRaw: String? = null

    fun offer(raw: String) {
        pendingRaw = raw
    }

    /** Lit et efface en une fois. */
    fun take(): String? {
        val v = pendingRaw
        pendingRaw = null
        return v
    }
}
