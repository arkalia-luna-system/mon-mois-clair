package be.monmoisclair.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SafFolderMessagesTest {

    @Test
    fun persistFailed_isFamilyPhrase_withoutSafJargon() {
        val msg = SafFolderMessages.PERSIST_FAILED
        assertThat(msg).contains("Documents")
        assertThat(msg).contains("Téléchargements")
        assertThat(msg).doesNotContain("SAF")
        assertThat(msg).doesNotContain("openFileDescriptor")
        assertThat(msg).doesNotContain("backup initial")
    }
}
