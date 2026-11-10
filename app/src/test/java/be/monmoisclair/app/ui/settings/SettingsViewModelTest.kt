package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun refreshFailedState_clearsLoadingAndShowsError() {
        val loading = SettingsUiState(loading = true)

        val failed = settingsRefreshFailedState(loading)

        assertThat(failed.loading).isFalse()
        assertThat(failed.flashMessage).isEqualTo(SETTINGS_REFRESH_FAILED)
        assertThat(failed.flashMessageError).isTrue()
    }
}
