package be.monmoisclair.app.domain

import android.content.Intent
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareFileIntentsTest {

    @Test
    fun pdf_grantsReadViaClipData() {
        val uri = Uri.parse("content://be.monmoisclair.app.fileprovider/share/resume.pdf")
        val intent = ShareFileIntents.pdf(uri, "Résumé — Mon Mois Clair")
        assertThat(intent.action).isEqualTo(Intent.ACTION_SEND)
        assertThat(intent.type).isEqualTo("application/pdf")
        @Suppress("DEPRECATION")
        val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        assertThat(stream).isEqualTo(uri)
        assertThat(intent.clipData).isNotNull()
        assertThat(intent.clipData!!.itemCount).isEqualTo(1)
        assertThat(intent.clipData!!.getItemAt(0).uri).isEqualTo(uri)
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isNotEqualTo(0)
        assertThat(intent.getStringExtra(Intent.EXTRA_TEXT)).isNull()
    }

    @Test
    fun pdf_withNote_explainsNotMmcBackup() {
        val uri = Uri.parse("content://be.monmoisclair.app.fileprovider/share/resume.pdf")
        val intent = ShareFileIntents.pdf(
            uri,
            "Résumé — Mon Mois Clair",
            text = ShareFileIntents.PDF_SUMMARY_SHARE_NOTE
        )
        val note = intent.getStringExtra(Intent.EXTRA_TEXT)
        assertThat(note).contains("clair")
        assertThat(note!!.lowercase()).contains(".mmc")
        assertThat(note.lowercase()).contains("pas une copie")
    }

    @Test
    fun zip_grantsReadViaClipData() {
        val uri = Uri.parse("content://be.monmoisclair.app.fileprovider/share/backup.zip")
        val intent = ShareFileIntents.zip(
            uri,
            "Sauvegarde Mon Mois Clair",
            "Copie de sécurité."
        )
        assertThat(intent.action).isEqualTo(Intent.ACTION_SEND)
        assertThat(intent.type).isEqualTo("application/zip")
        @Suppress("DEPRECATION")
        val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        assertThat(stream).isEqualTo(uri)
        assertThat(intent.getStringExtra(Intent.EXTRA_SUBJECT))
            .isEqualTo("Sauvegarde Mon Mois Clair")
        assertThat(intent.clipData).isNotNull()
        assertThat(intent.clipData!!.getItemAt(0).uri).isEqualTo(uri)
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isNotEqualTo(0)
    }

    @Test
    fun encryptedCopy_isOctetStream_notReadableZip() {
        val uri = Uri.parse("content://be.monmoisclair.app.fileprovider/share/copie.mmc")
        val intent = ShareFileIntents.encryptedCopy(
            uri,
            "Copie Mon Mois Clair",
            "Sans le code de copie, ce fichier ne peut pas être rouvert."
        )
        assertThat(intent.type).isEqualTo("application/octet-stream")
        assertThat(intent.getStringExtra(Intent.EXTRA_SUBJECT)).doesNotContain("chiffr")
        assertThat(intent.getStringExtra(Intent.EXTRA_TEXT)).contains("code de copie")
        @Suppress("DEPRECATION")
        assertThat(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)).isEqualTo(uri)
        assertThat(intent.clipData).isNotNull()
    }
}
