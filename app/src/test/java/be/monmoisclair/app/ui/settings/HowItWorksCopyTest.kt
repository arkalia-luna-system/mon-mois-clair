package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HowItWorksCopyTest {

    @Test
    fun sections_coverFamilyTopics() {
        val titles = HowItWorksCopy.sections.map { it.title }
        assertThat(titles).contains("Marquer FAIT")
        assertThat(titles).contains("Sauvegarde")
        assertThat(titles).contains("Alertes")
        assertThat(titles).contains("Courses")
        assertThat(titles).contains("En retard")
        assertThat(titles).contains("Facture papier")
        assertThat(titles).contains("Sur une année")
        assertThat(titles).contains("Retard ou accord")
        assertThat(titles.indexOf("Courses")).isLessThan(titles.indexOf("Marquer FAIT"))
        val all = HowItWorksCopy.sections.joinToString("\n") { "${it.title}\n${it.body}" }
        assertThat(all).contains("Reste du mois passé")
        assertThat(all).contains("Aussi les mois suivants")
        assertThat(all).contains("1 à 31")
        assertThat(all).contains("Ajouter")
        assertThat(all).contains("Après courses")
        assertThat(all).contains("Sans les futures")
        assertThat(all).contains("« Copier »")
        assertThat(all).contains("12 chiffres")
        assertThat(all).contains("Aussi les mois suivants")
        assertThat(all).contains("Réglages → Alertes")
        assertThat(all).contains("pastille « Accord »")
        assertThat(all).contains("IBAN")
        assertThat(all).contains("courriers")
        assertThat(all).doesNotContain("ancre")
        assertThat(all).doesNotContain("EN RETARD")
        assertThat(all).doesNotContain("+ Divers")
        assertThat(all).doesNotContain("Santé des rappels")
        assertThat(all).doesNotContain("Device Care")
        assertThat(all).doesNotContain("Menu → Alertes")
        assertThat(HowItWorksCopy.sections.size).isAtLeast(7)
        HowItWorksCopy.sections.forEach { s ->
            assertThat(s.body.length).isGreaterThan(40)
        }
    }
}
