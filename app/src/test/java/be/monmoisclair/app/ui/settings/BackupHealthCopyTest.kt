package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.model.BackupHealth
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupHealthCopyTest {

    @Test
    fun statusLine_coversAllStates() {
        assertThat(BackupHealthCopy.statusLine(BackupHealth.OK)).contains("OK")
        assertThat(BackupHealthCopy.statusLine(BackupHealth.BROKEN)).contains("à régler")
        assertThat(BackupHealthCopy.statusLine(BackupHealth.NOT_CONFIGURED))
            .contains("aucun dossier")
    }

    @Test
    fun isOk_onlyForOk() {
        assertThat(BackupHealthCopy.isOk(BackupHealth.OK)).isTrue()
        assertThat(BackupHealthCopy.isOk(BackupHealth.BROKEN)).isFalse()
        assertThat(BackupHealthCopy.isOk(BackupHealth.NOT_CONFIGURED)).isFalse()
    }
}
