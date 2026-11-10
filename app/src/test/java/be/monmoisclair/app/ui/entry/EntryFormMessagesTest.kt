package be.monmoisclair.app.ui.entry

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EntryFormMessagesTest {

    @Test
    fun defaultAttachmentErrors_stayShortAndNeutral() {
        assertThat(EntryFormMessages.PHOTO_DECODE_ERROR.lowercase())
            .doesNotContain("réessayez")
        assertThat(EntryFormMessages.PDF_READ_ERROR_DEFAULT.lowercase())
            .doesNotContain("réessayez")
        assertThat(EntryFormMessages.PHOTO_DECODE_ERROR).contains("photo")
        assertThat(EntryFormMessages.PDF_READ_ERROR_DEFAULT).contains("PDF")
    }
}
