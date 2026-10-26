package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.EuroFormat
import be.monmoisclair.app.domain.model.ReminderKind

/**
 * Textes notification paiements — pur JVM (masking biométrie testable).
 * Le [ReminderReceiver] affiche ce que le scheduler a déjà composé.
 */
object ReminderNotifCopy {

    const val MASKED_AMOUNT = "Montant masqué"

    fun amountLabel(amountCents: Long, hideAmounts: Boolean): String =
        if (hideAmounts) MASKED_AMOUNT else EuroFormat.formatCents(amountCents)

    fun title(kind: ReminderKind, label: String): String = when (kind) {
        ReminderKind.J3 -> "Dans 3 jours : $label"
        ReminderKind.JD -> "Aujourd’hui : $label"
        ReminderKind.OVERDUE -> "En retard : $label"
    }

    fun body(
        kind: ReminderKind,
        amountCents: Long,
        dayOfMonth: Int,
        isAutoDebit: Boolean,
        hideAmounts: Boolean
    ): String {
        val amount = amountLabel(amountCents, hideAmounts)
        val day = dayOfMonth
        val auto = isAutoDebit
        return when (kind) {
            ReminderKind.J3 ->
                if (auto) {
                    "$amount — le $day. Vérifiez le prélèvement."
                } else {
                    "$amount — le $day. Pensez à payer."
                }
            ReminderKind.JD ->
                if (auto) {
                    "$amount — vérifiez le prélèvement aujourd’hui (le $day)."
                } else {
                    "$amount — à payer aujourd’hui (le $day)."
                }
            ReminderKind.OVERDUE ->
                if (auto) {
                    "$amount — prélèvement prévu le $day non confirmé. Vérifiez votre compte."
                } else {
                    "$amount — le $day est passé. À régler."
                }
        }
    }
}
