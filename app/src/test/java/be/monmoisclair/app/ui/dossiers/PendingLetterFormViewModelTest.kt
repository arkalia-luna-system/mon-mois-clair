package be.monmoisclair.app.ui.dossiers

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PendingLetterFormViewModelTest {

    @Test
    fun saveFailedState_clearsLoadingAndShowsMessage() {
        val start = PendingLetterFormUiState(
            who = "Étude",
            what = "Hôpital",
            hasPendingPhoto = true,
            saving = true
        )

        val failed = pendingLetterSaveFailedState(start)

        assertThat(failed.saving).isFalse()
        assertThat(failed.error).isEqualTo(PENDING_LETTER_SAVE_FAILED)
        assertThat(failed.saved).isFalse()
    }
}
