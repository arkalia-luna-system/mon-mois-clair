package be.monmoisclair.app.data.backup

import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSnapshotReader @Inject constructor(
    private val monthLedgerDao: MonthLedgerDao,
    private val templateDao: TemplateDao,
    private val instanceDao: InstanceDao,
    private val appMetaDao: AppMetaDao,
    private val pendingLetterDao: PendingLetterDao
) {
    suspend fun readPayload(): BackupPayload {
        return BackupPayload(
            ledgers = monthLedgerDao.getAll(),
            incomeTemplates = templateDao.getAllIncomeTemplates(),
            expenseTemplates = templateDao.getAllExpenseTemplates(),
            incomeInstances = instanceDao.getAllIncomes(),
            expenseInstances = instanceDao.getAllExpenses(),
            appMeta = appMetaDao.getAll(),
            pendingLetters = pendingLetterDao.getAll()
        )
    }
}
