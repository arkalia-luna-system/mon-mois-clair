package be.monmoisclair.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity

@Database(
    entities = [
        MonthLedgerEntity::class,
        IncomeTemplateEntity::class,
        ExpenseTemplateEntity::class,
        IncomeInstanceEntity::class,
        ExpenseInstanceEntity::class,
        AppMetaEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monthLedgerDao(): MonthLedgerDao
    abstract fun templateDao(): TemplateDao
    abstract fun instanceDao(): InstanceDao
    abstract fun appMetaDao(): AppMetaDao
}
