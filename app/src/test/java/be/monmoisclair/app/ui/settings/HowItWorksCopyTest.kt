package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HowItWorksCopyTest {

    @Test
    fun sections_followSeniorJourney() {
        val titles = HowItWorksCopy.sections.map { it.title }
        assertThat(titles).contains("Première ouverture")
        assertThat(titles).contains("Noter une facture")
        assertThat(titles).contains("Marquer FAIT")
        assertThat(titles).contains("Sauvegarde")
        assertThat(titles).contains("Facture papier")
        assertThat(titles).contains("Copier pour la banque")
        assertThat(titles).contains("Plan de remboursement")
        assertThat(titles).contains("Résumé du mois")
        assertThat(titles).contains("Sur une année")
        assertThat(titles).contains("Alertes")
        assertThat(titles).contains("Courses")
        assertThat(titles).contains("En retard")
        assertThat(titles).contains("Pour aujourd’hui")
        assertThat(titles).contains("Nouveautés")
        assertThat(titles.indexOf("Première ouverture")).isEqualTo(0)
        assertThat(titles.indexOf("Noter une facture")).isEqualTo(1)
        assertThat(titles.indexOf("Marquer FAIT")).isEqualTo(2)
        assertThat(titles.indexOf("Sauvegarde")).isEqualTo(3)
        assertThat(titles.indexOf("Facture papier")).isEqualTo(4)
        assertThat(titles.indexOf("Copier pour la banque")).isEqualTo(5)
        assertThat(titles.indexOf("Plan de remboursement"))
            .isLessThan(titles.indexOf("Résumé du mois"))
        val all = HowItWorksCopy.sections.joinToString("\n") { "${it.title}\n${it.body}" }
        assertThat(all).contains("choisissez un dossier dans Documents")
        assertThat(all).contains("Depuis l’accueil, touchez +")
        assertThat(all).contains("Touchez Enregistrer")
        assertThat(all).contains("Appliquer aussi aux mois suivants")
        assertThat(all).contains("attendez le débit")
        assertThat(all).contains("Annuler ou Remettre")
        assertThat(all).contains("Réglages → Code et copie")
        assertThat(all).contains("Restaurer une sauvegarde")
        assertThat(all).contains("Lire la communication sur la photo")
        assertThat(all).contains("Joindre un PDF")
        assertThat(all.lowercase()).contains("presse-papiers")
        assertThat(all).contains("Menu → Dossiers")
        assertThat(all).contains("À traiter")
        assertThat(all).contains("Préparer un résumé")
        assertThat(all).contains("Choisir un mois")
        assertThat(all).contains("Après les FAIT")
        assertThat(all).contains("il reste")
        assertThat(all).contains("J’ai compris")
        assertThat(all).contains("Réglages → Confort → Alertes")
        assertThat(all).contains("À côté du Reste, touchez Courses")
        assertThat(all).contains("accord ou un courrier")
        assertThat(all).contains("pastille « Accord »")
        assertThat(all).contains("Mettre de côté")
        assertThat(all).contains("sous Courses")
        assertThat(all).contains("L’app ne garde pas d’argent")
        assertThat(all).doesNotContain("Menu → Courses")
        assertThat(all).doesNotContain("Menu → Mettre de côté")
        assertThat(all).contains("Mois à venir")
        assertThat(all).contains("tous les 3 mois")
        assertThat(all).contains("Échéances hors ce mois")
        assertThat(all).contains("À payer")
        assertThat(all.lowercase()).contains("n’envoyez jamais le code avec le fichier")
        assertThat(all.lowercase()).doesNotContain("chiffré")
        assertThat(all.lowercase()).doesNotContain("chiffrement")
        assertThat(all.lowercase()).doesNotContain("sqlcipher")
        assertThat(all.lowercase()).doesNotContain("aes-gcm")
        assertThat(all).doesNotContain("widget")
        assertThat(all).doesNotContain("Passer")
        assertThat(all).doesNotContain("Apparence")
        assertThat(all).doesNotContain("QuietLink")
        assertThat(all).doesNotContain("Un souci")
        assertThat(all).doesNotContain("Arkalia Système")
        assertThat(all).doesNotContain("Athalia")
        assertThat(all).doesNotContain("Device Care")
        assertThat(HowItWorksCopy.sections.size).isAtLeast(10)
        HowItWorksCopy.sections.forEach { s ->
            assertThat(s.body.length).isAtLeast(28)
            assertThat(s.body.length).isLessThan(360)
        }
        val papier = HowItWorksCopy.sections.first { it.title == "Facture papier" }.body
        assertThat(papier).contains("Lire la communication sur la photo")
        assertThat(papier).contains("Enregistrer")
        val accord = HowItWorksCopy.sections.first { it.title == "Plan de remboursement" }.body
        assertThat(accord).doesNotContain("IBAN")
        assertThat(accord).contains("facture devient un accord")
        assertThat(accord).contains("Accord")
        val retard = HowItWorksCopy.sections.first { it.title == "En retard" }.body
        assertThat(retard).contains("Mettre de côté")
        val annee = HowItWorksCopy.sections.first { it.title == "Sur une année" }.body
        assertThat(annee).doesNotContain("1 à 31")
        val resume = HowItWorksCopy.sections.first { it.title == "Résumé du mois" }.body
        assertThat(resume).contains("PDF")
        assertThat(resume).contains("Dossiers")
        assertThat(resume).contains("Réglages → Données & sauvegarde → Préparer un résumé")
        assertThat(resume).contains("accord ou un courrier")
        assertThat(all.lowercase()).doesNotContain(".mmc")
    }
}
