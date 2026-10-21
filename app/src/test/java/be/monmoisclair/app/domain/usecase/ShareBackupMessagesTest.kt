package be.monmoisclair.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShareBackupMessagesTest {

    @Test
    fun failed_isFixedFamilyPhrase() {
        val msg = ShareBackupMessages.FAILED
        assertThat(msg).startsWith("Impossible d’envoyer la copie.")
        assertThat(msg).contains("Documents")
        assertThat(msg).doesNotContain("BackupFailure")
        assertThat(msg).doesNotContain("Rename")
    }
}
