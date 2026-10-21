package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.repository.MutatingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeletePendingLetterUseCase @Inject constructor(
    private val mutatingRepository: MutatingRepository
) {
    suspend operator fun invoke(id: String) {
        mutatingRepository.mutate(rescheduleReminders = false) {
            pendingLetterDao.deleteById(id)
        }
    }
}
