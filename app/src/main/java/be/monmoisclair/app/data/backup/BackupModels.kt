package be.monmoisclair.app.data.backup

import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity

data class BackupPayload(
    val ledgers: List<MonthLedgerEntity>,
    val incomeTemplates: List<IncomeTemplateEntity>,
    val expenseTemplates: List<ExpenseTemplateEntity>,
    val incomeInstances: List<IncomeInstanceEntity>,
    val expenseInstances: List<ExpenseInstanceEntity>,
    val appMeta: List<AppMetaEntity>
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
    class NotConfigured : BackupFailure("Dossier SAF non configuré")
    class Io(message: String, cause: Throwable? = null) : BackupFailure(message, cause)
    class Integrity(message: String) : BackupFailure(message)
    class UnsupportedSchema(version: Int) : BackupFailure("schemaVersion non supporté: $version")
}
