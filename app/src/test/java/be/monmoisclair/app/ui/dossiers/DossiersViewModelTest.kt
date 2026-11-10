package be.monmoisclair.app.ui.dossiers

import be.monmoisclair.app.domain.usecase.DossiersSnapshot
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DossiersViewModelTest {

    @Test
    fun refreshFailedState_clearsLoadingAndShowsError() {
        val loading = DossiersUiState(
            loading = true,
            snapshot = DossiersSnapshot(emptyList(), 0L)
        )

        val failed = dossiersRefreshFailedState(loading)

        assertThat(failed.loading).isFalse()
        assertThat(failed.summaryMessage).isEqualTo(DOSSIERS_REFRESH_FAILED)
        assertThat(failed.summaryMessageError).isTrue()
    }
}
