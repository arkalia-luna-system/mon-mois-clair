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
    fun notConfigured_points_to_folder() {
        val msg = RestoreMessages.from(RestoreResult.NotConfigured)
        assertThat(msg).contains("dossier")
    }

    @Test
    fun unsupportedSchema_mentions_version() {
        val msg = RestoreMessages.from(RestoreResult.UnsupportedSchema(9))
        assertThat(msg).contains("9")
    }

    @Test
    fun invalidChecksum_mentions_integrity() {
        val msg = RestoreMessages.from(
            RestoreResult.InvalidChecksum(expected = "aa", actual = "bb")
        )
        assertThat(msg.lowercase()).contains("intégrité")
    }

    @Test
    fun ioError_stays_plain_language() {
        val msg = RestoreMessages.from(RestoreResult.IoError("disque plein"))
        assertThat(msg).doesNotContain("disque plein")
        assertThat(msg).contains("n’a pas été modifié")
    }
}
