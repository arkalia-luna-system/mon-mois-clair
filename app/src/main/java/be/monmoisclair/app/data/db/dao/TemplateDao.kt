package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity

@Dao
interface TemplateDao {

    @Query("SELECT * FROM income_templates WHERE active = 1")
    suspend fun activeIncomeTemplates(): List<IncomeTemplateEntity>

    @Query("SELECT * FROM expense_templates WHERE active = 1")
    suspend fun activeExpenseTemplates(): List<ExpenseTemplateEntity>

    @Query("SELECT * FROM income_templates WHERE id = :id")
    suspend fun getIncomeById(id: String): IncomeTemplateEntity?

    @Query("SELECT * FROM expense_templates WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseTemplateEntity?

    @Query("SELECT * FROM income_templates ORDER BY id ASC")
    suspend fun getAllIncomeTemplates(): List<IncomeTemplateEntity>

    @Query("SELECT * FROM expense_templates ORDER BY id ASC")
    suspend fun getAllExpenseTemplates(): List<ExpenseTemplateEntity>

    @Insert
    suspend fun insertIncome(t: IncomeTemplateEntity)

    @Insert
    suspend fun insertExpense(t: ExpenseTemplateEntity)

    @Update
    suspend fun updateIncome(t: IncomeTemplateEntity)

    @Update
    suspend fun updateExpense(t: ExpenseTemplateEntity)
}
