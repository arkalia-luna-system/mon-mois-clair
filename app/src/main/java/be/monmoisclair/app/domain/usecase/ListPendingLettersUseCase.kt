package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListPendingLettersUseCase @Inject constructor(
    private val pendingLetterDao: PendingLetterDao
) {
    suspend operator fun invoke(): List<PendingLetterEntity> = pendingLetterDao.getAll()
}
