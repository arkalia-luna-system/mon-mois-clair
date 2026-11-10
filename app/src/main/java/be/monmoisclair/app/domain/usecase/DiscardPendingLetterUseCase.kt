package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.repository.MutatingRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retire un courrier non converti : ligne DB + fichiers locaux liés (évite orphelins).
 */
@Singleton
class DiscardPendingLetterUseCase @Inject constructor(
    private val pendingLetterDao: PendingLetterDao,
    private val receiptPhotos: ReceiptPhotoStore,
    private val mutatingRepository: MutatingRepository
) {
    suspend operator fun invoke(letterId: String): Boolean {
        val letter = pendingLetterDao.getById(letterId) ?: return false
        mutatingRepository.mutate(rescheduleReminders = false) {
            pendingLetterDao.deleteById(letterId)
        }
        receiptPhotos.deleteLocalPaths(letter.receiptRelativePath)
        return true
    }
}
