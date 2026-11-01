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
            SettingsHubCopy.ALERTS,
            SettingsHubCopy.DEADLINES
        ).inOrder()
        assertThat(SettingsHubCopy.accountRows(showSummary = false)).containsExactly(
            SettingsHubCopy.BACKUP,
            SettingsHubCopy.SAVINGS,
            SettingsHubCopy.PRIVACY
        ).inOrder()
        assertThat(SettingsHubCopy.accountRows(showSummary = true))
            .containsExactly(
                SettingsHubCopy.BACKUP,
                SettingsHubCopy.SAVINGS,
                SettingsHubCopy.PRIVACY,
                SettingsHubCopy.SUMMARY
            ).inOrder()
        assertThat(SettingsHubCopy.BACKUP_SHORTCUT).contains("code")
        assertThat(SettingsHubCopy.HELP).contains("Comment ça marche")
    }

    @Test
    fun alwaysVisibleRows_coverCharteTopics() {
        assertThat(SettingsHubCopy.alwaysVisibleRows).containsExactly(
            SettingsHubCopy.APPEARANCE,
            SettingsHubCopy.SAVINGS,
            SettingsHubCopy.PRIVACY,
            SettingsHubCopy.ALERTS,
            SettingsHubCopy.DEADLINES,
            SettingsHubCopy.BACKUP,
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
            .isEqualTo(withSummary.indexOf(SettingsHubCopy.DEADLINES) + 1)
        assertThat(withSummary.indexOf(SettingsHubCopy.BACKUP))
            .isEqualTo(withSummary.indexOf(SettingsHubCopy.SUMMARY) + 1)
    }

    @Test
    fun alertsAndHelp_stayInHubNotHomeMenu() {
        // Charte §7 : Alertes / Aide / restore vivent dans Réglages.
        assertThat(SettingsHubCopy.alwaysVisibleRows).contains(SettingsHubCopy.ALERTS)
        assertThat(SettingsHubCopy.alwaysVisibleRows).contains(SettingsHubCopy.HELP)
        assertThat(SettingsHubCopy.alwaysVisibleRows).contains(SettingsHubCopy.BACKUP)
        assertThat(SettingsHubCopy.comfortRows()).contains(SettingsHubCopy.ALERTS)
        assertThat(SettingsHubCopy.accountRows(false)).contains(SettingsHubCopy.BACKUP)
        assertThat(SettingsHubCopy.hubGroups).contains(SettingsHubCopy.GROUP_HELP)
    }
}
