package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.domain.model.ExpenseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {

    @Query(
        "SELECT * FROM income_instances WHERE ledgerId = :ledgerId " +
            "ORDER BY dayOfMonth ASC, label ASC"
    )
    fun observeIncomes(ledgerId: String): Flow<List<IncomeInstanceEntity>>

    @Query(
        "SELECT * FROM expense_instances WHERE ledgerId = :ledgerId " +
            "ORDER BY dayOfMonth ASC, label ASC"
    )
    fun observeExpenses(ledgerId: String): Flow<List<ExpenseInstanceEntity>>

    @Query("SELECT * FROM income_instances WHERE id = :id")
    suspend fun getIncomeById(id: String): IncomeInstanceEntity?

    @Query("SELECT * FROM expense_instances WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseInstanceEntity?

    @Query("SELECT * FROM income_instances ORDER BY ledgerId ASC, dayOfMonth ASC, id ASC")
    suspend fun getAllIncomes(): List<IncomeInstanceEntity>

    @Query("SELECT * FROM expense_instances ORDER BY ledgerId ASC, dayOfMonth ASC, id ASC")
    suspend fun getAllExpenses(): List<ExpenseInstanceEntity>

    @Query(
        "SELECT * FROM expense_instances WHERE ledgerId = :ledgerId AND status = :status"
    )
    suspend fun expensesByStatus(
        ledgerId: String,
        status: ExpenseStatus
    ): List<ExpenseInstanceEntity>

    @Query("SELECT * FROM expense_instances WHERE status = :status")
    suspend fun allExpensesByStatus(status: ExpenseStatus): List<ExpenseInstanceEntity>

    @Query(
        "SELECT * FROM expense_instances WHERE templateId = :templateId " +
            "AND ledgerId >= :fromLedgerIdInclusive"
    )
    suspend fun expensesFromTemplateFromLedger(
        templateId: String,
        fromLedgerIdInclusive: String
    ): List<ExpenseInstanceEntity>

    @Query(
        "SELECT * FROM income_instances WHERE templateId = :templateId " +
            "AND ledgerId >= :fromLedgerIdInclusive"
    )
    suspend fun incomesFromTemplateFromLedger(
        templateId: String,
        fromLedgerIdInclusive: String
    ): List<IncomeInstanceEntity>

    @Insert
    suspend fun insertIncomes(items: List<IncomeInstanceEntity>)

    @Insert
    suspend fun insertExpenses(items: List<ExpenseInstanceEntity>)

    @Insert
    suspend fun insertIncome(item: IncomeInstanceEntity)

    @Insert
    suspend fun insertExpense(item: ExpenseInstanceEntity)

    @Update
    suspend fun updateExpense(item: ExpenseInstanceEntity)

    @Update
    suspend fun updateIncome(item: IncomeInstanceEntity)

    @Delete
    suspend fun deleteExpense(item: ExpenseInstanceEntity)

    @Delete
    suspend fun deleteIncome(item: IncomeInstanceEntity)
}
