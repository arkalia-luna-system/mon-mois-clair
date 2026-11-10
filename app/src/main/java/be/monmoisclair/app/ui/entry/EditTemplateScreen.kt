package be.monmoisclair.app.ui.entry

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTemplateScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EditTemplateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboardOpen = imeOpen()
    val labelBring = rememberFocusKeepsVisible()
    val amountBring = rememberFocusKeepsVisible()
    var confirmArchive by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    fun requestLeave() {
        if (state.edited) confirmLeave = true else onCancel()
    }

    BackHandler { requestLeave() }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = A11yTokens.PaperCard,
            title = {
                Text(
                    text = "Quitter sans enregistrer ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = "Les changements ne seront pas gardés.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Quitter",
                    onClick = {
                        confirmLeave = false
                        onCancel()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                    onClickLabel = "Quitter sans enregistrer"
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Rester",
                    onClick = { confirmLeave = false },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }

    if (confirmArchive) {
        val name = state.label.trim().ifBlank { "cette échéance" }
        AlertDialog(
            onDismissRequest = { confirmArchive = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = A11yTokens.PaperCard,
            title = {
                Text(
                    text = "Retirer $name ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = "L’échéance ne reviendra plus les mois suivants. " +
                        "Les mois déjà notés sur l’accueil ne changent pas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Retirer",
                    onClick = {
                        confirmArchive = false
                        viewModel.archive()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                    onClickLabel = "Retirer définitivement cette échéance"
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Annuler",
                    onClick = { confirmArchive = false },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }

    if (state.loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .padding(A11yTokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Chargement de l’échéance"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.Link,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Chargement…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
        }
        return
    }

    if (state.missing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .padding(A11yTokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CarnetPageTitle("Cette échéance n’est plus là.", withTopSpacer = false)
            Text(
                text = "Elle a peut-être déjà été retirée. Retournez à l’accueil ou aux Réglages.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            SecondaryA11yButton(text = "Retour", onClick = onCancel)
        }
        return
    }

    val fit = rememberDisplayFit()
    // Densité alignée EntryFormScaffold (One UI).
    val formGap = if (fit.compactHomeChrome) 14.dp else 18.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .imePadding()
            .padding(A11yTokens.screenPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(formGap)
        ) {
            CarnetPageTitle("Modifier l’échéance", withTopSpacer = false)
            // SYS01 : tip courant = texte InkMuted (pas de tip-box).
            Text(
                text = state.intervalHint,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Les mois déjà notés sur l’accueil ne changent pas. " +
                    "Les mois suivants suivront cette échéance.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.label,
                onValueChange = viewModel::setLabel,
                label = { Text("Nom") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(labelBring),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::setAmount,
                label = { Text("Montant (€)") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(amountBring),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            DayOfMonthGrid(
                selectedDay = state.dayText.toIntOrNull(),
                onSelect = { viewModel.setDay(it.toString()) }
            )
            CarnetSectionTitle("Mois de la prochaine échéance")
            Text(
                text = state.anchorYearLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.Ink
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (m in 1..12) {
                    TypeChip(
                        text = RecurrenceAnchor.frenchMonthLabel(m),
                        selected = state.anchorMonth == m,
                        onClick = { viewModel.setAnchorMonth(m) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        state.error?.let {
            CarnetStateBanner(text = it, kind = CarnetBannerKind.Error, pad = 12.dp)
            Spacer(Modifier.height(8.dp))
        }
        // Pied aligné EntryFormScaffold : divider + PaperCard + QuietLink Annuler.
        HorizontalDivider(color = A11yTokens.PaperBorder)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(A11yTokens.PaperCard)
                .padding(top = 12.dp)
        ) {
            PrimaryA11yButton(
                text = if (state.saving) "Enregistrement…" else "Enregistrer",
                onClick = viewModel::save,
                enabled = !state.saving
            )
            if (!keyboardOpen) {
                Spacer(Modifier.height(6.dp))
                QuietLinkA11y(
                    text = "Annuler",
                    onClick = { requestLeave() },
                    enabled = true,
                    fillMaxWidth = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClickLabel = "Annuler sans enregistrer"
                )
                Spacer(Modifier.height(6.dp))
                DangerA11yButton(
                    text = "Retirer cette échéance",
                    onClick = { confirmArchive = true },
                    enabled = !state.saving,
                    onClickLabel = "Demander le retrait de cette échéance"
                )
            }
        }
    }
}
