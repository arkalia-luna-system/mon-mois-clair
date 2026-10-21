package be.monmoisclair.app.data.backup

import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseFeeEventEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.db.entity.PendingLetterEntity

data class BackupPayload(
    val ledgers: List<MonthLedgerEntity>,
    val incomeTemplates: List<IncomeTemplateEntity>,
    val expenseTemplates: List<ExpenseTemplateEntity>,
    val incomeInstances: List<IncomeInstanceEntity>,
    val expenseInstances: List<ExpenseInstanceEntity>,
    val appMeta: List<AppMetaEntity>,
    /** Schéma 7+ ; vide pour les sauvegardes 1–6. */
    val pendingLetters: List<PendingLetterEntity> = emptyList(),
    /** Schéma 8+ ; vide pour les sauvegardes 1–7. */
    val feeEvents: List<ExpenseFeeEventEntity> = emptyList()
)

data class BackupDocument(
    val schemaVersion: Int,
    val exportedAtEpochMs: Long,
    val zoneId: String,
    val checksumSha256: String,
    val payload: BackupPayload
)

object BackupFileNames {
    const val TMP = "mon-mois-clair-backup.tmp"
    const val V1 = "mon-mois-clair-backup-1.json"
    const val V2 = "mon-mois-clair-backup-2.json"
    const val V3 = "mon-mois-clair-backup-3.json"

    val rotatedNewestFirst: List<String> = listOf(V1, V2, V3)
}

sealed class BackupFailure(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class NotConfigured : BackupFailure("Aucun dossier Documents choisi.")
    class Io(message: String, cause: Throwable? = null) : BackupFailure(message, cause)
    class Integrity(message: String) : BackupFailure(message)
    class UnsupportedSchema(val version: Int) :
        BackupFailure("Cette copie ne peut pas être lue (version $version).")
}

sealed class RestoreResult {
    data class Success(
        val checksumSha256: String,
        /** Photos attendues dans l’archive (0 si JSON seul). */
        val photosExpected: Int = 0,
        /** Photos réellement réécrites dans Documents. */
        val photosWritten: Int = 0
    ) : RestoreResult()

    data class InvalidChecksum(val expected: String, val actual: String) : RestoreResult()
    data class UnsupportedSchema(val version: Int) : RestoreResult()
    data class IoError(val message: String) : RestoreResult()
    data object NotConfigured : RestoreResult()
}
