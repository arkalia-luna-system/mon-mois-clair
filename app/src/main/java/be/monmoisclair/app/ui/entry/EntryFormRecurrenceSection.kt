package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.theme.A11yTokens

/** Section fréquence / prochaine échéance (fiche). */
@Composable
internal fun EntryFormRecurrenceSection(
    showRecurring: Boolean,
    showFrequencyEdit: Boolean,
    recurrence: RecurrenceChoice,
    onRecurrence: ((RecurrenceChoice) -> Unit)?,
    recurrenceInfo: String?,
    nextDueMonth: Int,
    onNextDueMonth: (Int) -> Unit,
    showNextDueMonth: Boolean,
    recurring: Boolean,
    onRecurring: (Boolean) -> Unit
) {
    // Édition ponctuelle (sans modèle) : info seule.
    if (!showRecurring && !showFrequencyEdit) {
        recurrenceInfo?.let { info ->
            Text(text = "Fréquence", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
            Text(text = info, style = MaterialTheme.typography.bodyLarge)
        }
    }

    if ((showRecurring || showFrequencyEdit) && onRecurrence != null) {
        CarnetSectionTitle("Fréquence")
        if (showFrequencyEdit) {
            Text(
                text = "Mois déjà créés : cochez « Appliquer aussi aux mois suivants » " +
                    "lors de l’enregistrement.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypeChip(RecurrenceLabel.chipOnce(), selected = recurrence == RecurrenceChoice.Once) {
                onRecurrence(RecurrenceChoice.Once)
            }
            TypeChip(RecurrenceLabel.chipMonthly(), selected = recurrence == RecurrenceChoice.Monthly) {
                onRecurrence(RecurrenceChoice.Monthly)
            }
            TypeChip(
                RecurrenceLabel.chipEvery3Months(),
                selected = recurrence == RecurrenceChoice.Every3Months
            ) {
                onRecurrence(RecurrenceChoice.Every3Months)
            }
            TypeChip(RecurrenceLabel.chipYearly(), selected = recurrence == RecurrenceChoice.Yearly) {
                onRecurrence(RecurrenceChoice.Yearly)
            }
        }
        if (showNextDueMonth) {
            CarnetSectionTitle("Prochaine échéance")
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (m in 1..12) {
                    TypeChip(
                        text = be.monmoisclair.app.domain.RecurrenceAnchor.frenchMonthLabel(m),
                        selected = nextDueMonth == m
                    ) {
                        onNextDueMonth(m)
                    }
                }
            }
        }
    } else if (showRecurring) {
        CheckRow(
            checked = recurring,
            label = RecurrenceLabel.forIntervalMonths(1),
            onChecked = onRecurring
        )
    }
}
