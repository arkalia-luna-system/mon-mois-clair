package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsHubCopyTest {

    @Test
    fun hubGroups_areThreeCalmDoors() {
        assertThat(SettingsHubCopy.hubGroups).containsExactly(
            SettingsHubCopy.GROUP_ACCOUNT,
            SettingsHubCopy.GROUP_COMFORT,
            SettingsHubCopy.GROUP_HELP
        ).inOrder()
    }

    @Test
    fun comfortAndAccountRows_coverCharteTopics() {
        assertThat(SettingsHubCopy.comfortRows()).containsExactly(
            SettingsHubCopy.APPEARANCE,
            SettingsHubCopy.ALERTS
        ).inOrder()
        assertThat(SettingsHubCopy.comfortRows())
            .doesNotContain(SettingsHubCopy.DEADLINES)
        assertThat(SettingsHubCopy.accountRows(showSummary = false)).containsExactly(
            SettingsHubCopy.BACKUP,
            SettingsHubCopy.DEADLINES,
            SettingsHubCopy.SAVINGS_ROW,
            SettingsHubCopy.PRIVACY
        ).inOrder()
        assertThat(SettingsHubCopy.accountRows(showSummary = true))
            .containsExactly(
                SettingsHubCopy.BACKUP,
                SettingsHubCopy.DEADLINES,
                SettingsHubCopy.SAVINGS_ROW,
                SettingsHubCopy.PRIVACY,
                SettingsHubCopy.SUMMARY
            ).inOrder()
        assertThat(SettingsHubCopy.BACKUP_SHORTCUT).contains("Code")
        assertThat(SettingsHubCopy.HELP).isEqualTo("Comment ça marche")
        assertThat(SettingsHubCopy.GROUP_HELP).contains(SettingsHubCopy.HELP)
        assertThat(SettingsHubCopy.GROUP_HELP).contains("Aide")
        // Hub ≠ Accueil sticky : précise jour / retrait.
        assertThat(SettingsHubCopy.SAVINGS_ROW).contains("Jour")
        assertThat(SettingsHubCopy.SAVINGS_ROW).isNotEqualTo(SettingsHubCopy.SAVINGS)
    }

    @Test
    fun alwaysVisibleRows_coverCharteTopics() {
        assertThat(SettingsHubCopy.alwaysVisibleRows).containsExactly(
            SettingsHubCopy.APPEARANCE,
            SettingsHubCopy.ALERTS,
            SettingsHubCopy.BACKUP,
            SettingsHubCopy.DEADLINES,
            SettingsHubCopy.SAVINGS_ROW,
            SettingsHubCopy.PRIVACY,
            SettingsHubCopy.HELP
        ).inOrder()
    }

    @Test
    fun hubRows_insertsSummaryWhenNeeded() {
        assertThat(SettingsHubCopy.hubRows(showSummary = false))
            .doesNotContain(SettingsHubCopy.SUMMARY)
        val withSummary = SettingsHubCopy.hubRows(showSummary = true)
        assertThat(withSummary).contains(SettingsHubCopy.SUMMARY)
        assertThat(withSummary.indexOf(SettingsHubCopy.SUMMARY))
            .isEqualTo(withSummary.indexOf(SettingsHubCopy.PRIVACY) + 1)
        assertThat(withSummary.indexOf(SettingsHubCopy.DEADLINES))
            .isEqualTo(withSummary.indexOf(SettingsHubCopy.BACKUP) + 1)
    }

    @Test
    fun alertsAndHelp_stayAvailableInSettings() {
        assertThat(SettingsHubCopy.alwaysVisibleRows).contains(SettingsHubCopy.ALERTS)
        assertThat(SettingsHubCopy.alwaysVisibleRows).contains(SettingsHubCopy.HELP)
        assertThat(SettingsHubCopy.alwaysVisibleRows).contains(SettingsHubCopy.BACKUP)
        assertThat(SettingsHubCopy.comfortRows()).contains(SettingsHubCopy.ALERTS)
        assertThat(SettingsHubCopy.accountRows(false)).contains(SettingsHubCopy.BACKUP)
        assertThat(SettingsHubCopy.accountRows(false)).contains(SettingsHubCopy.DEADLINES)
        assertThat(SettingsHubCopy.hubGroups).contains(SettingsHubCopy.GROUP_HELP)
    }
}
