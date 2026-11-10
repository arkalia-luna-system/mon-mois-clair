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

    @Test
    fun ok_withEpoch_showsLastCopyDate() {
        // 2026-11-10 12:00 Europe/Brussels (test JVM : AppClock.zone)
        val epoch = 1_762_670_400_000L
        val line = BackupHealthCopy.statusLine(BackupHealth.OK, epoch)
        assertThat(line).contains("à jour")
        assertThat(line).contains("Dernière copie")
        assertThat(line).contains("/")
    }

    @Test
    fun broken_ignoresEpoch() {
        val line = BackupHealthCopy.statusLine(BackupHealth.BROKEN, 1_762_670_400_000L)
        assertThat(line).doesNotContain("Dernière copie")
    }
}
