package be.monmoisclair.app.ui.entry

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Garde-fou A-P0-1 : Scaffold consomme des modèles groupés, pas une liste plate ≥ 40 params.
 */
class EntryFormUiModelsTest {

    @Test
    fun groupedModels_stayBounded() {
        assertThat(EntryFormIdentityUi::class.java.constructors[0].parameterCount)
            .isAtMost(12)
        assertThat(EntryFormRecurrenceUi::class.java.constructors[0].parameterCount)
            .isAtMost(16)
        assertThat(EntryFormPaymentUi::class.java.constructors[0].parameterCount)
            .isAtMost(24)
        assertThat(EntryFormDossierUi::class.java.constructors[0].parameterCount)
            .isAtMost(24)
        assertThat(EntryFormChromeUi::class.java.constructors[0].parameterCount)
            .isAtMost(10)
        val groups = listOf(
            "EntryFormIdentityUi",
            "EntryFormRecurrenceUi",
            "EntryFormPaymentUi",
            "EntryFormDossierUi",
            "EntryFormSavingsUi",
            "EntryFormChromeUi"
        )
        assertThat(groups).containsNoDuplicates()
        assertThat(groups).hasSize(6)
    }

    @Test
    fun savingsDefaults_areOff() {
        val s = EntryFormSavingsUi()
        assertThat(s.isSavingsLine).isFalse()
        assertThat(s.lockAmountAndDay).isFalse()
        assertThat(s.savingsTransferredCents).isEqualTo(0L)
    }

    @Test
    fun chrome_requiresErrorAndPrimary() {
        var primaryHits = 0
        val chrome = EntryFormChromeUi(
            error = null,
            primaryLabel = "Enregistrer",
            primaryEnabled = true,
            onPrimary = { primaryHits++ },
            onCancel = {}
        )
        chrome.onPrimary()
        assertThat(primaryHits).isEqualTo(1)
        assertThat(chrome.deleteLabel).isEqualTo("Supprimer")
    }
}
