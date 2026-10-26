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
        val photosWritten: Int = 0,
        /**
         * Restore d’une ancienne copie ZIP/JSON **claire** (pré-6.81)
         * via opt-in explicite [BackupRestorer.restoreFromUri] `allowCleartextLegacy`.
         */
        val cleartextGrace: Boolean = false
    ) : RestoreResult()

    data class InvalidChecksum(val expected: String, val actual: String) : RestoreResult()
    data class UnsupportedSchema(val version: Int) : RestoreResult()
    data class IoError(val message: String) : RestoreResult()
    /** Copie chiffrée : code manquant ou incorrect. */
    data class BadShareCode(val message: String = DEFAULT_BAD_CODE) : RestoreResult()
    /**
     * ZIP/JSON clair refusé par défaut (post-6.85).
     * Réessayer avec l’opt-in « ancien format » dans Restaurer.
     */
    data object CleartextRejected : RestoreResult()
    data object NotConfigured : RestoreResult()

    companion object {
        const val DEFAULT_BAD_CODE =
            "Ce code ne permet pas d’ouvrir la copie. " +
                "Vérifiez les 8 chiffres, ou rouvrez depuis le téléphone qui a créé la copie."
    }
}
