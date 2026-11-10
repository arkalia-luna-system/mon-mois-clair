package be.monmoisclair.app.domain

import be.monmoisclair.app.domain.EuroFormat

/**
 * Détection d’une facture « presque pareille » (même mois) pour demander
 * confirmation avant création — évite les doublons par mégarde ou double Enregistrer.
 */
object SimilarExpenseMatcher {

    data class Candidate(
        val label: String,
        val amountCents: Long,
        val dayOfMonth: Int,
        /** Créancier / cause (accord) pour rapprocher les libellés. */
        val creditorHint: String = ""
    )

    data class Match(
        val existingLabel: String,
        val dayOfMonth: Int,
        val amountCents: Long
    )

    fun normalizeLabel(raw: String): String =
        raw.trim().lowercase().replace(Regex("\\s+"), " ")

    /**
     * Libellés proches : identiques ou l’un contient l’autre (min. 4 caractères).
     */
    fun labelsSimilar(a: String, b: String): Boolean {
        val na = normalizeLabel(a)
        val nb = normalizeLabel(b)
        if (na.isEmpty() || nb.isEmpty()) return false
        if (na == nb) return true
        if (na.length < 4 || nb.length < 4) return false
        return na in nb || nb in na
    }

    fun isSimilar(new: Candidate, existing: Candidate): Boolean {
        if (new.amountCents != existing.amountCents) return false
        if (new.dayOfMonth != existing.dayOfMonth) return false
        val against = listOf(existing.label, existing.creditorHint).filter { it.isNotBlank() }
        if (against.any { labelsSimilar(new.label, it) }) return true
        val mergedNew = listOf(new.label, new.creditorHint).filter { it.isNotBlank() }.joinToString(" ")
        return against.any { labelsSimilar(mergedNew, it) }
    }

    fun confirmDialogBody(match: Match): String {
        val money = EuroFormat.formatCents(match.amountCents)
        return "Une ligne « ${match.existingLabel.trim()} » existe déjà " +
            "pour le jour ${match.dayOfMonth} · $money.\n\n" +
            "Voulez-vous quand même enregistrer une nouvelle ligne ?"
    }
}
