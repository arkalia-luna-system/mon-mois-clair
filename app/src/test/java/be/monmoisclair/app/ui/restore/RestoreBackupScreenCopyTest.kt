package be.monmoisclair.app.ui.restore

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RestoreBackupScreenCopyTest {

    @Test
    fun selectedFolderCopy_staysGeneric() {
        assertThat(RESTORE_SELECTED_FOLDER_KEPT.lowercase()).contains("cet appareil")
        assertThat(RESTORE_SELECTED_FOLDER_KEPT.lowercase()).contains("conservé")
        assertThat(RESTORE_SELECTED_FOLDER_KEPT.lowercase()).doesNotContain("téléphone")
    }
}
