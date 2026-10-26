package be.monmoisclair.app.domain

import android.content.ClipData
import android.content.Intent
import android.net.Uri

/** Intents de partage fichier — toujours avec [ClipData] pour la permission URI. */
object ShareFileIntents {

    fun pdf(uri: Uri, subject: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

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
