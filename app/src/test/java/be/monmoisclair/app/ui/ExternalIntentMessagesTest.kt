package be.monmoisclair.app.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExternalIntentMessagesTest {

    @Test
    fun shareAndPdfMessages_staySpecific() {
        assertThat(ExternalIntentMessages.NO_SHARE_APP).contains("fichier")
        assertThat(ExternalIntentMessages.NO_SHARE_APP.lowercase()).doesNotContain("réessayez")
        assertThat(ExternalIntentMessages.NO_PDF_VIEWER).contains("PDF")
    }
}
