package be.monmoisclair.app.ui.scan

/**
 * Passage QR → fiche **en mémoire seulement**.
 * Évite de laisser l’IBAN / la référence dans [androidx.lifecycle.SavedStateHandle]
 * (persistance / process death).
 */
object EpcScanHandoff {
    private val pendingRaw = java.util.concurrent.atomic.AtomicReference<String?>(null)

    fun offer(raw: String) {
        pendingRaw.set(raw)
    }

    /** Lit et efface en une fois (atomique). */
    fun take(): String? = pendingRaw.getAndSet(null)
}
