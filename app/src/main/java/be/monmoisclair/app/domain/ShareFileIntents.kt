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
}
