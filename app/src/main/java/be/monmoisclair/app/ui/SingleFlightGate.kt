package be.monmoisclair.app.ui

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Une seule opération « en vol » (save, restore, convert…) — anti-double-tap / coroutines parallèles.
 */
class SingleFlightGate {
    private val inFlight = AtomicBoolean(false)

    fun tryEnter(): Boolean = inFlight.compareAndSet(false, true)

    fun exit() {
        inFlight.set(false)
    }
}
