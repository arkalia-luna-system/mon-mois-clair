package be.monmoisclair.app.ui

import java.util.concurrent.atomic.AtomicInteger

/**
 * Garde-fou pour les chargements async (ViewModel) :
 * seule la dernière requête [startLoad] a le droit de publier l’état UI.
 */
class AsyncLoadCoordinator {
    private val generation = AtomicInteger(0)

    fun startLoad(): Int = generation.incrementAndGet()

    fun isStale(loadId: Int): Boolean = loadId != generation.get()
}
