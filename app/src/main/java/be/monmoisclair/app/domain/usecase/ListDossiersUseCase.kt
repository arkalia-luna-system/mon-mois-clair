package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.model.ExpenseStatus
import javax.inject.Inject
import javax.inject.Singleton

data class DossierListItem(
    val templateId: String,
    val who: String,
    val what: String,
    /** Versement prévu ce mois (ligne du mois ou forfait). */
    val monthCents: Long,
    val remainingCents: Long,
    val totalDueCents: Long,
    val paidCents: Long,
    val isSettled: Boolean,
    /** Instance à ouvrir (mois courant si possible, sinon la plus récente). */
    val openInstanceId: String?
)

data class DossiersSnapshot(
    val items: List<DossierListItem>,
    /** Somme des versements du mois pour les dossiers encore ouverts. */
    val monthTotalCents: Long
) {
    val count: Int get() = items.size
}

/**
 * Lot D — liste des accords (modèles avec reste de départ).
 * 0 dossier → liste vide (pas d’entrée Menu).
 */
@Singleton
class ListDossiersUseCase @Inject constructor(
    private val templateDao: TemplateDao,
    private val instanceDao: InstanceDao
) {
    suspend operator fun invoke(): DossiersSnapshot {
        val currentYm = AppClock.currentYearMonth().toString()
        val items = mutableListOf<DossierListItem>()
        var monthTotal = 0L
        for (tpl in templateDao.activeExpenseTemplates()) {
            if (!DossierRemaining.isDossier(tpl.openingRemainingCents)) continue
            val paid = instanceDao.sumAmountCentsByTemplateAndStatus(
                tpl.id,
                ExpenseStatus.DONE
            )
            val fees = tpl.addedFeesCents
            val remaining = DossierRemaining.remainingCents(
                tpl.openingRemainingCents,
                paid,
                fees
            ) ?: continue
            val totalDue = (tpl.openingRemainingCents ?: 0L) + fees.coerceAtLeast(0L)
            val settled = remaining <= 0L
            val instances = instanceDao.expensesByTemplate(tpl.id)
            val thisMonth = instances.firstOrNull { it.ledgerId == currentYm }
            val openId = thisMonth?.id
                ?: instances.maxByOrNull { it.ledgerId }?.id
            val monthCents = when {
                thisMonth != null -> thisMonth.amountCents
                settled -> 0L
                else -> tpl.amountCents
            }
            // « Ce mois » = une ligne vraiment dans le mois, pas le forfait d’une échéance lointaine.
            if (!settled && thisMonth != null) monthTotal += thisMonth.amountCents
            items += DossierListItem(
                templateId = tpl.id,
                who = tpl.label,
                what = tpl.causeLabel,
                monthCents = monthCents,
                remainingCents = remaining.coerceAtLeast(0L),
                totalDueCents = totalDue.coerceAtLeast(0L),
                paidCents = paid.coerceAtLeast(0L),
                isSettled = settled,
                openInstanceId = openId
            )
        }
        // Ouverts d’abord, puis soldés ; alpha sur le nom.
        val ordered = items.sortedWith(
            compareBy<DossierListItem> { it.isSettled }
                .thenBy { it.who.lowercase() }
        )
        return DossiersSnapshot(items = ordered, monthTotalCents = monthTotal)
    }
}
