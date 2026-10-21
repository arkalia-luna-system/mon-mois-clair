package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lot D — courrier reçu sans versement fixé.
 * N’entre pas dans le Reste et ne crée pas de ligne de mois.
 * Dès qu’un versement + reste dû sont saisis → conversion en dossier (template).
 */
@Entity(tableName = "pending_letters")
data class PendingLetterEntity(
    @PrimaryKey val id: String,
    /** À qui (créancier). */
    val who: String,
    /** Pour quoi. */
    val what: String = "",
    /** Photo `receipts/….webp` (peut être vide). */
    val receiptRelativePath: String = "",
    val paymentNote: String = "",
    val iban: String = "",
    val creditorEmail: String = "",
    val createdAtEpochMs: Long
)
