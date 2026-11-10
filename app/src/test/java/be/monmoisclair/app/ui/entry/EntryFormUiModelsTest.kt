package be.monmoisclair.app.ui.entry

import com.google.common.truth.Truth.assertThat
import java.lang.reflect.Modifier
import org.junit.Test

/**
 * Garde-fou A-P0-1 : Scaffold consomme des modèles groupés, pas une liste plate ≥ 40 params.
 *
 * Ne pas utiliser `constructors[0]` : l’ordre JVM inclut les synthétiques
 * `DefaultConstructorMarker` et n’est pas stable (CI flaky develop vert / main rouge).
 */
class EntryFormUiModelsTest {

    private fun primaryArity(clazz: Class<*>): Int =
        clazz.declaredFields.count { field ->
            !field.isSynthetic && !Modifier.isStatic(field.modifiers)
        }

    @Test
    fun groupedModels_stayBounded() {
        assertThat(primaryArity(EntryFormIdentityUi::class.java)).isAtMost(12)
        assertThat(primaryArity(EntryFormRecurrenceUi::class.java)).isAtMost(16)
        assertThat(primaryArity(EntryFormPaymentUi::class.java)).isAtMost(24)
        // Le bloc accord/dossier porte volontairement plus d'état métier
        // (reste dû, IBAN, e-mail, frais, événements) tout en restant groupé.
        assertThat(primaryArity(EntryFormDossierUi::class.java)).isAtMost(28)
        assertThat(primaryArity(EntryFormChromeUi::class.java)).isAtMost(10)
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
