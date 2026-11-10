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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.DiscardChangesDialog
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.ui.components.PinnedActionScaffold
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.entry.DayOfMonthGrid
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun ConvertPendingLetterScreen(
    onBack: () -> Unit,
    onConverted: (instanceId: String?) -> Unit,
    viewModel: ConvertPendingLetterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPadLg
    var confirmLeave by remember { mutableStateOf(false) }
    val dirty = !state.loading &&
        (state.openingRemainingText.isNotBlank() ||
            state.monthlyAmountText.isNotBlank())

    fun requestLeave() {
        if (dirty && !state.done) confirmLeave = true else onBack()
    }

    BackHandler(enabled = !state.loading && !state.done) { requestLeave() }
    if (confirmLeave) {
        DiscardChangesDialog(
            onStay = { confirmLeave = false },
            onLeave = {
                confirmLeave = false
                onBack()
            }
        )
    }

    LaunchedEffect(state.done) {
        if (state.done) onConverted(state.createdInstanceId)
    }

    // Chargement / courrier disparu : Retour seul (pas d’action primaire).
    if (state.loading || (state.who.isBlank() && state.error != null)) {
        PinnedBackScaffold(onBack = onBack) {
            CarnetPageTitle("Fixer un versement", withTopSpacer = false)
            if (state.loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tipPad)
                        .semantics {
                            contentDescription = "Chargement du courrier"
                            liveRegion = LiveRegionMode.Polite
                        }
                ) {
                    CircularProgressIndicator(
                        color = A11yTokens.Link,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Chargement…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted
                    )
                }
            } else {
                // Un seul PageTitle ; erreur = titre Ink + détail muted.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(
                        if (fit.compactHomeChrome) 6.dp else 8.dp
                    )
                ) {
                    Text(
                        text = state.error ?: "Ce courrier n’est plus là.",
                        style = MaterialTheme.typography.titleLarge,
                        color = A11yTokens.Ink,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Déjà converti ou retiré.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        return
    }

    // Créer + Annuler épinglés : la grille 1–31 ne doit pas manger l’action
    // sous le clavier (campagne P2 / SM-S938B).
    PinnedActionScaffold(
        primaryLabel = if (state.saving) "Enregistrement…" else "Créer l’accord",
        onPrimary = viewModel::convert,
        primaryEnabled = !state.saving && !state.loading,
        secondaryLabel = "Annuler",
        onSecondary = { requestLeave() },
        secondaryEnabled = !state.saving
    ) {
        CarnetPageTitle("Fixer un versement", withTopSpacer = false)
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
        CarnetTipCard(
            text = "Le versement entre dans le Reste. IBAN et e-mail : ensuite dans la fiche."
        )

        OutlinedTextField(
            value = state.openingRemainingText,
            onValueChange = viewModel::setOpeningRemaining,
            label = { Text("Reste dû aujourd’hui") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = carnetOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
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
            colors = carnetOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            enabled = !state.saving && !state.loading
        )
        DayOfMonthGrid(
            selectedDay = state.dayText.trim().toIntOrNull()?.takeIf { it in 1..31 },
            onSelect = { viewModel.setDay(it.toString()) },
            enabled = !state.saving && !state.loading
        )
        state.error?.let { err ->
            CarnetStateBanner(text = err, kind = CarnetBannerKind.Error, pad = tipPad)
        }
        if (state.saving) {
            Text(
                text = "Création de l’accord…",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Création de l’accord"
                    }
            )
        }
    }
}
