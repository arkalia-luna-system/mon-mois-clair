package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.usecase.DossierListItem

/**
 * Jointure chip « encore ~N » : par templateId (M−1 / M+1),
 * repli openInstanceId (mois courant).
 */
object HomeTimelineDossierJoin {

    fun remainingPaymentsFor(
        rowTemplateId: String?,
        rowInstanceId: String,
        dossiers: List<DossierListItem>
    ): Int? {
        if (dossiers.isEmpty()) return null
        val tid = rowTemplateId?.trim().orEmpty()
        if (tid.isNotEmpty()) {
            dossiers.firstOrNull { it.templateId == tid }?.remainingPayments?.let { return it }
        }
        return dossiers.firstOrNull { it.openInstanceId == rowInstanceId }?.remainingPayments
    }
}
