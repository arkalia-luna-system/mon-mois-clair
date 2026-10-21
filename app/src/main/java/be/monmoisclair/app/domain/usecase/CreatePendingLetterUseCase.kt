package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class CreatePendingLetterResult {
    data class Success(val id: String) : CreatePendingLetterResult()
    data class InvalidInput(val message: String) : CreatePendingLetterResult()
}

/**
 * Range un courrier sans inventer de montant (pas de ligne de mois, pas dans le Reste).
 */
@Singleton
class CreatePendingLetterUseCase @Inject constructor(
    private val mutatingRepository: MutatingRepository
) {
    suspend operator fun invoke(
        who: String,
        what: String = "",
        receiptRelativePath: String = "",
        paymentNote: String = "",
        iban: String = "",
        creditorEmail: String = ""
    ): CreatePendingLetterResult {
        val trimmedWho = who.trim()
        if (trimmedWho.isEmpty()) {
            return CreatePendingLetterResult.InvalidInput("Indiquez à qui (le créancier).")
        }
        val id = "pending-${UUID.randomUUID()}"
        val now = AppClock.nowEpochMs()
        mutatingRepository.mutate(rescheduleReminders = false) {
            pendingLetterDao.upsert(
                PendingLetterEntity(
                    id = id,
                    who = trimmedWho,
                    what = what.trim(),
                    receiptRelativePath = receiptRelativePath.trim(),
                    paymentNote = paymentNote.trim(),
                    iban = iban.trim(),
                    creditorEmail = creditorEmail.trim(),
                    createdAtEpochMs = now
                )
            )
        }
        return CreatePendingLetterResult.Success(id)
    }
}
