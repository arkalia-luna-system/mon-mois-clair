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
        assertThat(titles).contains("Pour aujourd’hui")
        assertThat(titles).contains("Facture papier")
        assertThat(titles).contains("Sur une année")
        assertThat(titles).contains("Plan de remboursement")
        assertThat(titles).contains("Résumé du mois")
        // P1-U4 : FAIT + Sauvegarde en tête.
        assertThat(titles.indexOf("Marquer FAIT")).isEqualTo(0)
        assertThat(titles.indexOf("Sauvegarde")).isEqualTo(1)
        assertThat(titles.indexOf("Sauvegarde")).isLessThan(titles.indexOf("Courses"))
        val all = HowItWorksCopy.sections.joinToString("\n") { "${it.title}\n${it.body}" }
        assertThat(all).contains("Reste du mois passé")
        assertThat(all).contains("Appliquer aussi aux mois suivants")
        assertThat(all).contains("Doublon")
        assertThat(all).contains("Pour aujourd’hui")
        assertThat(all).contains("À traiter")
        assertThat(all).contains("il reste")
        assertThat(all).contains("Après les FAIT")
        assertThat(all).contains("Lire la communication sur la photo")
        assertThat(all).contains("jamais le montant")
        assertThat(all).contains("Prélèvement : cochez quand vous voyez le débit")
        assertThat(all).contains("Réglages → Confort → Alertes")
        assertThat(all).contains("Accueil, à côté du Reste : icône chariot Courses")
        assertThat(all).contains("quand un accord ou un courrier existe")
        assertThat(all).contains("pastille « Accord »")
        assertThat(all).contains("Menu → Dossiers")
        assertThat(all).contains("Mettre de côté")
        assertThat(all).contains("à côté du Reste (sous Courses)")
        assertThat(all).contains("Pas d’argent dans l’app")
        assertThat(all).doesNotContain("Menu → Courses")
        assertThat(all).doesNotContain("Menu → Mettre de côté")
        assertThat(all).contains("Détails")
        assertThat(all).contains("À venir")
        assertThat(all).contains("tous les 3 mois")
        assertThat(all).contains("Échéances hors ce mois")
        assertThat(all).contains("Préparer un résumé")
        assertThat(all).contains("À payer")
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
        assertThat(all).doesNotContain("QuietLink")
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
        val accord = HowItWorksCopy.sections.first { it.title == "Plan de remboursement" }.body
        assertThat(accord).doesNotContain("IBAN")
        assertThat(accord).contains("pas la pastille")
        assertThat(accord).contains("accord de paiement")
        assertThat(accord).contains("Voir le rythme")
        val retard = HowItWorksCopy.sections.first { it.title == "En retard" }.body
        assertThat(retard).contains("Mettre de côté")
        val annee = HowItWorksCopy.sections.first { it.title == "Sur une année" }.body
        assertThat(annee).doesNotContain("1 à 31")
        val resume = HowItWorksCopy.sections.first { it.title == "Résumé du mois" }.body
        assertThat(resume).contains("PDF")
        assertThat(resume).contains("Dossiers")
        assertThat(resume).contains("Réglages → Données → Préparer un résumé")
        assertThat(resume).contains("Si un accord ou un courrier existe")
        assertThat(all.lowercase()).doesNotContain(".mmc")
    }
}
