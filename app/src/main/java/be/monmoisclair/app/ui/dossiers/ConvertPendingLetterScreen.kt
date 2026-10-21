package be.monmoisclair.app.ui.dossiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.entry.DayOfMonthGrid
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun ConvertPendingLetterScreen(
    onBack: () -> Unit,
    onConverted: (instanceId: String?) -> Unit,
    viewModel: ConvertPendingLetterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    LaunchedEffect(state.done) {
        if (state.done) onConverted(state.createdInstanceId)
    }

    PinnedBackScaffold(onBack = onBack) {
        Text(
            text = "Fixer un versement",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        if (state.loading) {
            val busyShape = MaterialTheme.shapes.medium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperCard, busyShape)
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Chargement du courrier"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.ButtonFill,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Chargement…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
            return@PinnedBackScaffold
        }
        if (state.who.isBlank() && state.error != null) {
            val missShape = MaterialTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, missShape)
                    .background(A11yTokens.PaperCard, missShape)
                    .padding(if (fit.compactHomeChrome) 12.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(
                    if (fit.compactHomeChrome) 6.dp else 8.dp
                )
            ) {
                Text(
                    text = state.error ?: "Ce courrier n’est plus là.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = A11yTokens.Ink
                )
                Text(
                    text = "Il a peut-être déjà été converti ou retiré.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
            return@PinnedBackScaffold
        }
        val tipShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, tipShape)
                .background(A11yTokens.PaperCard, tipShape)
                .padding(tipPad),
            verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 4.dp else 6.dp)
        ) {
            Text(
                text = state.who.ifBlank { "Courrier" },
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            if (state.what.isNotBlank()) {
                Text(
                    text = state.what,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
            Text(
                text = "Le courrier devient un accord : le versement entre dans le Reste du mois. " +
                    "IBAN et e-mail se remplissent ensuite dans la fiche.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
            OutlinedTextField(
                value = state.openingRemainingText,
                onValueChange = viewModel::setOpeningRemaining,
                label = { Text("Combien reste-t-il aujourd’hui ?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !state.saving && !state.loading
            )
            OutlinedTextField(
                value = state.monthlyAmountText,
                onValueChange = viewModel::setMonthlyAmount,
                label = { Text("Versement chaque mois") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !state.saving && !state.loading
            )
            DayOfMonthGrid(
                selectedDay = state.dayText.trim().toIntOrNull()?.takeIf { it in 1..31 },
                onSelect = { viewModel.setDay(it.toString()) },
                enabled = !state.saving && !state.loading
            )
            state.error?.let { err ->
                val errShape = MaterialTheme.shapes.medium
                Text(
                    err,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.AccentDanger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.AccentDanger.copy(alpha = 0.4f), errShape)
                        .background(A11yTokens.PaperWarm, errShape)
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Assertive
                            contentDescription = err
                        }
                )
            }
            if (state.saving) {
                val busyShape = MaterialTheme.shapes.medium
                Text(
                    text = "Création de l’accord…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, busyShape)
                        .background(A11yTokens.PaperCard, busyShape)
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Création de l’accord"
                        }
                )
            }
            PrimaryA11yButton(
                text = if (state.saving) "Enregistrement…" else "Créer l’accord",
                onClick = viewModel::convert,
                enabled = !state.saving && !state.loading
            )
            SecondaryA11yButton(
                text = "Annuler",
                onClick = onBack,
                enabled = !state.saving
            )
        }
    }
}
