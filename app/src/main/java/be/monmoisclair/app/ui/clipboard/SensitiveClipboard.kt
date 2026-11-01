package be.monmoisclair.app.ui.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle

/**
 * Presse-papiers pour IBAN / communication : marque le contenu comme sensible
 * (Android 13+) et l’efface après un court délai.
 */
object SensitiveClipboard {

    /** Fenêtre courte : IBAN / +++ ne restent pas longtemps hors app. */
    internal const val CLEAR_AFTER_MS = 30_000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null

    fun setPlainText(context: Context, label: String, text: String) {
        val clip = ClipData.newPlainText(label, text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(clip)
        scheduleClear(cm)
    }

    private fun scheduleClear(cm: ClipboardManager) {
        clearRunnable?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                runCatching { cm.clearPrimaryClip() }
            }
        }
        clearRunnable = runnable
        mainHandler.postDelayed(runnable, CLEAR_AFTER_MS)
    }
}
