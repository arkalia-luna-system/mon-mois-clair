package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeMenuCopyTest {

    @Test
    fun visibleRows_keepHelpAboveSupport() {
        assertThat(HomeMenuCopy.visibleRows).containsExactly(
            HomeMenuCopy.SETTINGS,
            HomeMenuCopy.UPCOMING,
            HomeMenuCopy.PICK_MONTH,
            HomeMenuCopy.HELP
        ).inOrder()
        assertThat(HomeMenuCopy.HELP).contains("Aide")
        assertThat(HomeMenuCopy.HELP).contains("Comment ça marche")
        assertThat(HomeMenuCopy.SUPPORT).contains("Un souci")
        assertThat(HomeMenuCopy.UPCOMING).isEqualTo("Mois à venir")
    }
}
