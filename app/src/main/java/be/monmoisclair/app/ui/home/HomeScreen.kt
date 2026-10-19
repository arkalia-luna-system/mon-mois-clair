package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onEdit: (id: String, isExpense: Boolean) -> Unit,
    onOpenHealth: () -> Unit,
    onOpenSettingsSaf: () -> Unit,
    onOpenRestore: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
    ) {
        Surface(
            color = A11yTokens.PaperWarm,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(A11yTokens.screenPadding)) {
                Text(text = "Mon Mois Clair", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reste à vivre",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = MoneyFormat.formatCents(state.summary.remainingCents),
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Encore à payer : " +
                        MoneyFormat.formatCents(state.summary.stillToPayCents),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹ Préc.",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.canGoPrev) A11yTokens.Ink else A11yTokens.InkMuted,
                        modifier = Modifier
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(enabled = state.canGoPrev) { viewModel.prevMonth() }
                            .padding(8.dp)
                    )
                    Text(
                        text = state.monthLabel,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "Suiv. ›",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.canGoNext) A11yTokens.Ink else A11yTokens.InkMuted,
                        modifier = Modifier
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(enabled = state.canGoNext) { viewModel.nextMonth() }
                            .padding(8.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = A11yTokens.screenPadding,
                end = A11yTokens.screenPadding,
                top = A11yTokens.screenPadding,
                // Marge basse liste : derniers boutons pas collés à la zone système.
                bottom = A11yTokens.screenPadding + A11yTokens.bottomSystemClearance
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.timeline.isEmpty() && !state.loading) {
                item {
                    Text(
                        text = "Aucune entrée ce mois-ci. Ajoutez un revenu ou une dépense.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(state.timeline, key = { item ->
                when (item) {
                    is TimelineItem.Income -> "i-${item.id}"
                    is TimelineItem.Expense -> "e-${item.id}"
                }
            }) { item ->
                when (item) {
                    is TimelineItem.Income -> IncomeRow(
                        item = item,
                        onClick = { onEdit(item.id, false) }
                    )
                    is TimelineItem.Expense -> ExpenseRow(
                        item = item,
                        onToggle = { viewModel.toggleDone(item.id) },
                        onClick = { onEdit(item.id, true) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryA11yButton(text = "Ajouter une entrée", onClick = onAdd)
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(text = "Santé des rappels", onClick = onOpenHealth)
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(text = "Dossier de sauvegarde", onClick = onOpenSettingsSaf)
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(text = "Restaurer une sauvegarde", onClick = onOpenRestore)
            }
        }
    }
}

@Composable
private fun IncomeRow(item: TimelineItem.Income, onClick: () -> Unit) {
    Surface(
        color = A11yTokens.PaperWarm,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight)
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Text(text = item.dayLabel, style = MaterialTheme.typography.bodyMedium)
            Text(text = item.label, style = MaterialTheme.typography.titleLarge)
            Text(
                text = item.amountLabel,
                color = A11yTokens.AccentOk,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ExpenseRow(
    item: TimelineItem.Expense,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val bg = if (item.isDone) A11yTokens.AccentOk.copy(alpha = 0.18f) else A11yTokens.PaperWarm
    Surface(
        color = bg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Zone « modifier » séparée du bouton FAIT (évite double action).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.dayLabel, style = MaterialTheme.typography.bodyMedium)
                    if (item.isDone) {
                        Text(
                            text = "FAIT ✓",
                            color = A11yTokens.AccentOk,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Text(text = item.label, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = item.amountLabel,
                    color = if (item.isDone) A11yTokens.InkMuted else A11yTokens.Ink,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryA11yButton(
                text = if (item.isDone) "Marquer à payer" else "Marquer FAIT ✓",
                onClick = onToggle
            )
        }
    }
}
