package be.monmoisclair.app.ui.entry

import android.graphics.Bitmap

/**
 * Brouillon pièces jointes partagé Add / Edit — bitmap + PDF en mémoire
 * avant enregistrement. Pas de logique métier ici.
 */
class EntryFormReceiptDraft {
    @Volatile
    private var pendingBitmap: Bitmap? = null

    @Volatile
    private var pendingPdfBytes: ByteArray? = null

    fun attachBitmap(b: Bitmap) {
        pendingBitmap = b
    }

    fun attachPdf(bytes: ByteArray) {
        pendingPdfBytes = bytes
    }

    fun clearPdf() {
        pendingPdfBytes = null
    }

    fun clearBitmap() {
        pendingBitmap = null
    }

    fun clearAll() {
        pendingBitmap = null
        pendingPdfBytes = null
    }

    fun peekBitmap(): Bitmap? = pendingBitmap

    fun peekPdf(): ByteArray? = pendingPdfBytes

    fun takeBitmap(): Bitmap? {
        val b = pendingBitmap
        pendingBitmap = null
        return b
    }

    fun takePdf(): ByteArray? {
        val p = pendingPdfBytes
        pendingPdfBytes = null
        return p
    }

    val hasPhoto: Boolean get() = pendingBitmap != null
    val hasPdf: Boolean get() = pendingPdfBytes != null
}
