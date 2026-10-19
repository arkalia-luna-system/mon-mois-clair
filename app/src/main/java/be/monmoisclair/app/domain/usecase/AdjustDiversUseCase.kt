package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthCycleEngine
import javax.inject.Inject
import javax.inject.Singleton

sealed class AdjustDiversResult {
    data class Success(val diversSpentCents: Long) : AdjustDiversResult()
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
            return AdjustDiversResult.Failed("Montant nul.")
        }
        val ym = runCatching { java.time.YearMonth.parse(ledgerId) }.getOrElse {
            return AdjustDiversResult.Failed("Mois invalide.")
        }
        monthCycleEngine.ensureMonthExists(ym)
        return mutatingRepository.mutate(rescheduleReminders = false) {
            val ledger = monthLedgerDao.getById(ledgerId)
                ?: return@mutate AdjustDiversResult.Failed("Mois introuvable.")
            val next = (ledger.diversSpentCents + deltaCents).coerceAtLeast(0L)
            monthLedgerDao.updateDiversSpent(ledgerId, next)
            AdjustDiversResult.Success(next)
        }
    }
}
