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
        assertThat(all).contains("J’ai dépensé")
        assertThat(all).contains("Après courses")
        assertThat(all).contains("Courses")
        assertThat(all).contains("Après les FAIT")
        assertThat(all).contains("« Copier »")
        assertThat(all).contains("12 chiffres")
        assertThat(all).contains("Aussi les mois suivants")
        assertThat(all).contains("Réglages → Alertes")
        assertThat(all).contains("pastille « Accord »")
        assertThat(all).contains("IBAN")
        assertThat(all).contains("courriers")
        assertThat(all).contains("Mes dossiers")
        assertThat(all).contains("Mettre de côté")
        assertThat(all).contains("argent gardé")
        assertThat(all).contains("Déjà viré")
        assertThat(all).contains("Voir la photo")
        assertThat(all).contains("petit écran")
        assertThat(all).contains("Ranger sans plan")
        assertThat(all).contains("Préparer un résumé")
        assertThat(all).contains("app Mail")
        assertThat(all).contains("encore environ N mois si")
        assertThat(all).contains("Vers mois année")
        assertThat(all).contains("Soldés")
        assertThat(all).contains("replié")
        assertThat(all).contains("À venir")
        assertThat(all).contains("aperçu lecture seule")
        assertThat(all).contains("tous les 3 mois")
        assertThat(all).contains("Un souci avec l’app")
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
