package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.MonthNavigation
import javax.inject.Inject
import javax.inject.Singleton

sealed class AdjustDiversResult {
    data class Success(val diversSpentCents: Long) : AdjustDiversResult()

    /** Retrait plus grand que le total : tout a été remis à zéro (P3-F03). */
    data class ReducedToZero(
        val previousCents: Long
    ) : AdjustDiversResult()

    data class Failed(val message: String) : AdjustDiversResult()
}

@Singleton
class AdjustDiversUseCase @Inject constructor(
    private val monthCycleEngine: MonthCycleEngine,
    private val mutatingRepository: MutatingRepository
) {
    /**
     * @param deltaCents positif = ajouter une dépense divers ; négatif = retirer.
     * Le total divers ne descend pas sous 0.
     */
    suspend operator fun invoke(ledgerId: String, deltaCents: Long): AdjustDiversResult {
        if (deltaCents == 0L) {
            return AdjustDiversResult.Failed("Indiquez un montant.")
        }
        val ym = runCatching { java.time.YearMonth.parse(ledgerId) }.getOrElse {
            return AdjustDiversResult.Failed("Ce mois n’est pas disponible.")
        }
        if (MonthNavigation.isFuturePreview(ym, AppClock.currentYearMonth())) {
            return AdjustDiversResult.Failed(
                "Aperçu seulement — les courses se notent sur le mois en cours."
            )
        }
        monthCycleEngine.ensureMonthExists(ym)
        return mutatingRepository.mutate(rescheduleReminders = false) {
            val ledger = monthLedgerDao.getById(ledgerId)
                ?: return@mutate AdjustDiversResult.Failed("Ce mois n’est pas disponible.")
            val current = ledger.diversSpentCents
            if (deltaCents < 0L && -deltaCents > current) {
                // P3-F03 : ne pas faire croire qu’on a retiré le montant tapé.
                monthLedgerDao.updateDiversSpent(ledgerId, 0L)
                return@mutate AdjustDiversResult.ReducedToZero(previousCents = current)
            }
            val next = current + deltaCents
            monthLedgerDao.updateDiversSpent(ledgerId, next)
            AdjustDiversResult.Success(next)
        }
    }
}
