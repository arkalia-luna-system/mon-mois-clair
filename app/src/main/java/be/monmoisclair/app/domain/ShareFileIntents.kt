package be.monmoisclair.app.domain

import android.content.ClipData
import android.content.Intent
import android.net.Uri

/** Intents de partage fichier — toujours avec [ClipData] pour la permission URI. */
object ShareFileIntents {

    fun pdf(uri: Uri, subject: String, text: String? = null): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            if (!text.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    /** Texte d’accompagnement PDF résumé — clair ≠ copie de sauvegarde (P1 UX sécu). */
    const val PDF_SUMMARY_SHARE_NOTE =
        "Résumé Mon Mois Clair (PDF clair, lecture immédiate). " +
            "Ce n’est pas une copie de sauvegarde — ne remplace pas votre code de copie."

    fun zip(uri: Uri, subject: String, text: String): Intent =
        file(uri, subject, text, mime = "application/zip")

    /** Copie chiffrée `.mmc` (enveloppe AES-GCM) — pas un ZIP lisible. */
    fun encryptedCopy(uri: Uri, subject: String, text: String): Intent =
        file(uri, subject, text, mime = "application/octet-stream")

    private fun file(uri: Uri, subject: String, text: String, mime: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}
