package be.monmoisclair.app.ui.restore

import be.monmoisclair.app.data.backup.RestoreResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RestoreMessagesTest {

    @Test
    fun success_message_is_positive() {
        val msg = RestoreMessages.from(RestoreResult.Success("abc"))
        assertThat(msg).contains("réussie")
    }

    @Test
    fun success_warns_when_photos_missing() {
        val msg = RestoreMessages.from(
            RestoreResult.Success(
                checksumSha256 = "abc",
                photosExpected = 2,
                photosWritten = 0
            )
        )
        assertThat(msg).contains("photos")
        assertThat(msg).contains("Documents")
    }

    @Test
    fun success_mentions_photos_when_all_written() {
        val msg = RestoreMessages.from(
            RestoreResult.Success(
                checksumSha256 = "abc",
                photosExpected = 2,
                photosWritten = 2
            )
        )
        assertThat(msg).contains("2 photos")
    }

    @Test
    fun notConfigured_points_to_folder() {
        val msg = RestoreMessages.from(RestoreResult.NotConfigured)
        assertThat(msg).contains("dossier")
    }

    @Test
    fun unsupportedSchema_stays_plain() {
        val msg = RestoreMessages.from(RestoreResult.UnsupportedSchema(9))
        assertThat(msg).doesNotContain("9")
        assertThat(msg).contains("n’a pas été modifié")
    }

    @Test
    fun invalidChecksum_stays_plain() {
        val msg = RestoreMessages.from(
            RestoreResult.InvalidChecksum(expected = "aa", actual = "bb")
        )
        assertThat(msg.lowercase()).contains("abîmée")
        assertThat(msg).doesNotContain("intégrité")
    }

    @Test
    fun ioError_stays_plain_language() {
        val msg = RestoreMessages.from(RestoreResult.IoError("disque plein"))
        assertThat(msg).doesNotContain("disque plein")
        assertThat(msg).contains("n’a pas été modifié")
    }

    @Test
    fun badShareCode_plain_language() {
        val msg = RestoreMessages.from(RestoreResult.BadShareCode())
        assertThat(msg.lowercase()).contains("code")
        assertThat(msg).contains("8 chiffres")
        assertThat(msg.lowercase()).doesNotContain("aes")
        assertThat(msg.lowercase()).doesNotContain("gcm")
        assertThat(msg.lowercase()).doesNotContain("passphrase")
    }

    @Test
    fun cleartextGrace_warnsStrongly() {
        val msg = RestoreMessages.from(
            RestoreResult.Success(checksumSha256 = "abc", cleartextGrace = true)
        )
        assertThat(msg).contains("réussie")
        assertThat(msg).contains(RestoreMessages.CLEARTEXT_GRACE_WARNING)
        assertThat(msg.lowercase()).contains("non protégée")
        assertThat(msg.lowercase()).contains("jet")
        assertThat(msg.lowercase()).doesNotContain("chiffr")
    }

    @Test
    fun cleartextRejected_pointsToNewProtectedCopy() {
        val msg = RestoreMessages.from(RestoreResult.CleartextRejected)
        assertThat(msg).isEqualTo(RestoreMessages.CLEARTEXT_REJECTED)
        assertThat(msg.lowercase()).contains("non protégée")
        assertThat(msg.lowercase()).contains("n’est plus acceptée")
        assertThat(msg.lowercase()).contains("sauvegarde")
        assertThat(msg).contains("n’a pas été modifié")
        assertThat(msg.lowercase()).contains("ancien téléphone")
        assertThat(msg.lowercase()).doesNotContain("chiffr")
        assertThat(msg.lowercase()).doesNotContain("ancien format")
        assertThat(msg).doesNotContain("6.81")
        assertThat(msg.lowercase()).doesNotContain(".mmc")
    }

    @Test
    fun notMmcBackup_dedicatedMessage() {
        val msg = RestoreMessages.from(RestoreResult.NotMmcBackup)
        assertThat(msg).isEqualTo(RestoreMessages.NOT_MMC_BACKUP)
        assertThat(msg).contains("n’est pas une copie Mon Mois Clair")
        assertThat(msg.lowercase()).contains("photo")
        assertThat(msg).contains("n’a pas été modifié")
        assertThat(msg.lowercase()).doesNotContain("non protégée")
    }
}
