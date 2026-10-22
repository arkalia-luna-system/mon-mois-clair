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
        assertThat(all).contains("Après courses")
        assertThat(all).contains("Après les FAIT")
        assertThat(all).contains("Lire +++ sur la photo")
        assertThat(all).contains("jamais le montant")
        assertThat(all).contains("Réglages → Alertes")
        assertThat(all).contains("pastille « Accord »")
        assertThat(all).contains("Mes dossiers")
        assertThat(all).contains("Mettre de côté")
        assertThat(all).contains("Pas d’argent dans l’app")
        assertThat(all).contains("À venir")
        assertThat(all).contains("tous les 3 mois")
        assertThat(all).contains("Échéances hors ce mois")
        assertThat(all).contains("Envoyer une copie")
        assertThat(all).contains("Restaurer")
        // Sortis de l’aide (vitent ailleurs / trop longs) :
        assertThat(all).doesNotContain("widget")
        assertThat(all).doesNotContain("Apparence")
        assertThat(all).doesNotContain("Un souci avec l’app")
        assertThat(all).doesNotContain("encore environ N mois si")
        assertThat(all).doesNotContain("Vers mois année")
        assertThat(all).doesNotContain("Ranger sans plan")
        assertThat(all).doesNotContain("petit écran")
        assertThat(all).doesNotContain("preuve rassurante")
        assertThat(all).doesNotContain("ancre")
        assertThat(all).doesNotContain("EN RETARD")
        assertThat(all).doesNotContain("+ Divers")
        assertThat(all).doesNotContain("Santé des rappels")
        assertThat(all).doesNotContain("Device Care")
        assertThat(all).doesNotContain("Menu → Alertes")
        assertThat(HowItWorksCopy.sections.size).isAtLeast(7)
        HowItWorksCopy.sections.forEach { s ->
            assertThat(s.body.length).isAtLeast(30)
        }
        val papier = HowItWorksCopy.sections.first { it.title == "Facture papier" }.body
        assertThat(papier.length).isLessThan(420)
        assertThat(papier).doesNotContain("Voir la photo")
        val accord = HowItWorksCopy.sections.first { it.title == "Retard ou accord" }.body
        assertThat(accord.length).isLessThan(420)
        assertThat(accord).doesNotContain("IBAN")
        val annee = HowItWorksCopy.sections.first { it.title == "Sur une année" }.body
        assertThat(annee.length).isLessThan(350)
        assertThat(annee).doesNotContain("1 à 31")
    }
}
