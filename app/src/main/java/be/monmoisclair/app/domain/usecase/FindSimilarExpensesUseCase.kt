package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.SimilarExpenseMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindSimilarExpensesUseCase @Inject constructor(
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao
) {

    suspend fun findInLedger(
        ledgerId: String,
        label: String,
        amountCents: Long,
        dayOfMonth: Int,
        causeLabel: String = ""
    ): List<SimilarExpenseMatcher.Match> {
        val candidate = SimilarExpenseMatcher.Candidate(
            label = label,
            amountCents = amountCents,
            dayOfMonth = dayOfMonth,
            creditorHint = causeLabel
        )
        return instanceDao.getExpensesForLedger(ledgerId).mapNotNull { row ->
            val hint = row.templateId?.let { templateDao.getExpenseById(it)?.causeLabel }.orEmpty()
            val other = SimilarExpenseMatcher.Candidate(
                label = row.label,
                amountCents = row.amountCents,
                dayOfMonth = row.dayOfMonth,
                creditorHint = hint
            )
            if (SimilarExpenseMatcher.isSimilar(candidate, other)) {
                SimilarExpenseMatcher.Match(
                    existingLabel = row.label,
                    dayOfMonth = row.dayOfMonth,
                    amountCents = row.amountCents
                )
            } else {
                null
            }
        }
    }
}
