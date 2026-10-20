package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.model.BackupHealth
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupHealthCopyTest {

    @Test
    fun statusLine_coversAllStates() {
        assertThat(BackupHealthCopy.statusLine(BackupHealth.OK)).contains("à jour")
        assertThat(BackupHealthCopy.statusLine(BackupHealth.BROKEN)).contains("ne s’écrit plus")
        assertThat(BackupHealthCopy.statusLine(BackupHealth.NOT_CONFIGURED))
            .contains("Aucun dossier")
    }

    @Test
    fun isOk_onlyForOk() {
        assertThat(BackupHealthCopy.isOk(BackupHealth.OK)).isTrue()
        assertThat(BackupHealthCopy.isOk(BackupHealth.BROKEN)).isFalse()
        assertThat(BackupHealthCopy.isOk(BackupHealth.NOT_CONFIGURED)).isFalse()
    }
}
