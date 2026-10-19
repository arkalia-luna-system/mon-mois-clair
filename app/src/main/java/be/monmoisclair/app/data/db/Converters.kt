package be.monmoisclair.app.data.db

import androidx.room.TypeConverter
import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.domain.model.ExpenseStatus

class Converters {

    @TypeConverter
    fun fromExpenseStatus(value: ExpenseStatus): String = value.name

    @TypeConverter
    fun toExpenseStatus(value: String): ExpenseStatus = ExpenseStatus.valueOf(value)

    @TypeConverter
    fun fromBackupHealth(value: BackupHealth): String = value.name

    @TypeConverter
    fun toBackupHealth(value: String): BackupHealth = BackupHealth.valueOf(value)
}
