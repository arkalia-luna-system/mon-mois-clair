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
        // P1-U4 : FAIT + Sauvegarde en tête.
        assertThat(titles.indexOf("Marquer FAIT")).isEqualTo(0)
        assertThat(titles.indexOf("Sauvegarde")).isEqualTo(1)
        assertThat(titles.indexOf("Sauvegarde")).isLessThan(titles.indexOf("Courses"))
        val all = HowItWorksCopy.sections.joinToString("\n") { "${it.title}\n${it.body}" }
        assertThat(all).contains("Reste du mois passé")
        assertThat(all).contains("Aussi les mois suivants")
        assertThat(all).contains("Après courses")
        assertThat(all).contains("Après les FAIT")
        assertThat(all).contains("Lire la communication sur la photo")
        assertThat(all).contains("jamais le montant")
        assertThat(all).contains("Prélèvement : cochez quand vous voyez le débit")
        assertThat(all).contains("Réglages → Alertes")
        assertThat(all).contains("pastille « Accord »")
        assertThat(all).contains("Menu → Dossiers")
        assertThat(all).contains("Mettre de côté")
        assertThat(all).contains("Pas d’argent dans l’app")
        assertThat(all).contains("Détails")
        assertThat(all).contains("À venir")
        assertThat(all).contains("tous les 3 mois")
        assertThat(all).contains("Échéances hors ce mois")
        assertThat(all).contains("Restaurer")
        assertThat(all).contains("Réglages → Sauvegarde")
        assertThat(all.lowercase()).contains("n’envoyez jamais le code avec la copie")
        assertThat(all.lowercase()).contains("vieilles copies non protégées")
        assertThat(all.lowercase()).doesNotContain("chiffré")
        assertThat(all.lowercase()).doesNotContain("chiffrement")
        assertThat(all.lowercase()).doesNotContain("sqlcipher")
        assertThat(all.lowercase()).doesNotContain("aes-gcm")
        assertThat(all).doesNotContain("widget")
        assertThat(all).doesNotContain("Apparence")
        assertThat(all).doesNotContain("Un souci")
        assertThat(all).doesNotContain("Arkalia Système")
        assertThat(all).doesNotContain("Athalia")
        assertThat(all).doesNotContain("Device Care")
        assertThat(HowItWorksCopy.sections.size).isAtLeast(7)
        HowItWorksCopy.sections.forEach { s ->
            assertThat(s.body.length).isAtLeast(28)
            assertThat(s.body.length).isLessThan(320)
        }
        val papier = HowItWorksCopy.sections.first { it.title == "Facture papier" }.body
        assertThat(papier).doesNotContain("Voir la photo")
        val accord = HowItWorksCopy.sections.first { it.title == "Retard ou accord" }.body
        assertThat(accord).doesNotContain("IBAN")
        assertThat(accord).contains("pas la pastille")
        assertThat(accord).contains("plan de remboursement")
        val retard = HowItWorksCopy.sections.first { it.title == "En retard" }.body
        assertThat(retard).contains("Mettre de côté")
        val annee = HowItWorksCopy.sections.first { it.title == "Sur une année" }.body
        assertThat(annee).doesNotContain("1 à 31")
    }
}
