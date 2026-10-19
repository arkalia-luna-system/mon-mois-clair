package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_templates")
data class ExpenseTemplateEntity(
    @PrimaryKey val id: String,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val active: Boolean,
    val remindJ3: Boolean = true,
    val remindJd: Boolean = true,
    /** Prélèvement automatique (notif « vérifier »). */
    val isAutoDebit: Boolean = false,
    /** 1 = mensuel, 3 = trimestriel, 12 = annuel, etc. */
    val intervalMonths: Int = 1,
    /** Premier mois d’application (`yyyy-MM`). Vide = traité comme mois de création à la volée. */
    val anchorYearMonth: String = "",
    val createdAtEpochMs: Long,
    val archivedAtEpochMs: Long?
)
