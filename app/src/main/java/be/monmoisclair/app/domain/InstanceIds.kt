package be.monmoisclair.app.domain

import java.util.UUID

/**
 * Identifiants d'instances.
 * Pour les occurrences issues d'un template : id déterministe (ledger + template)
 * afin d'éviter tout doublon si une génération était rejouée par erreur.
 */
object InstanceIds {

    fun incomeFromTemplate(ledgerId: String, templateId: String): String =
        "inc:$ledgerId:$templateId"

    fun expenseFromTemplate(ledgerId: String, templateId: String): String =
        "exp:$ledgerId:$templateId"

    fun oneOffIncome(): String = "inc:oneoff:${UUID.randomUUID()}"

    fun oneOffExpense(): String = "exp:oneoff:${UUID.randomUUID()}"
}
