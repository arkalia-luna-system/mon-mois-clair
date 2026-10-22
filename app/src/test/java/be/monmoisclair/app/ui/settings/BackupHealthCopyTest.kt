package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.model.BackupHealth
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupHealthCopyTest {

    @Test
    fun notConfigured_doesNotClaimAutoCopy() {
        val line = BackupHealthCopy.statusLine(BackupHealth.NOT_CONFIGURED)
        assertThat(line).isEqualTo("Aucun dossier Documents choisi.")
        assertThat(line.lowercase()).doesNotContain("automat")
        assertThat(BackupHealthCopy.isOk(BackupHealth.NOT_CONFIGURED)).isFalse()
    }

    @Test
    fun ok_and_broken_are_distinct() {
        assertThat(BackupHealthCopy.isOk(BackupHealth.OK)).isTrue()
        assertThat(BackupHealthCopy.statusLine(BackupHealth.OK)).contains("à jour")
        assertThat(BackupHealthCopy.statusLine(BackupHealth.BROKEN)).contains("dossier")
    }
}
