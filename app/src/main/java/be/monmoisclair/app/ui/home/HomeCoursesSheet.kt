package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSheetHandle
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance

/**
 * Unique UI d’édition Courses / divers — ouverte depuis la bannière sticky.
 * Le Reste après factures ne change pas (hors ledger factures).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCoursesSheet(
    diversSpentCents: Long,
    afterDiversCents: Long,
    input: String,
    onInputChange: (String) -> Unit,
    error: String?,
    busy: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fit = rememberDisplayFit()
    val sheetBottom = rememberSheetBottomClearance()
    val fieldBring = rememberFocusKeepsVisible()
    val gap = if (fit.compactHomeChrome) 8.dp else 12.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { CarnetSheetHandle() },
        containerColor = A11yTokens.PaperCream,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = sheetBottom)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = A11yTokens.screenPadding)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            CarnetPageTitle("Courses", withTopSpacer = false, shortSheet = true)
            val statusLine = when {
                diversSpentCents > 0L && afterDiversCents < 0L ->
                    "Noté ${MoneyFormat.formatCents(diversSpentCents)} · il reste " +
                        "${MoneyFormat.formatCents(afterDiversCents)}"
                diversSpentCents > 0L ->
                    "Noté ${MoneyFormat.formatCents(diversSpentCents)} · hors du reste du mois"
                else -> "Hors du reste du mois"
            }
            Text(
                text = statusLine,
                style = MaterialTheme.typography.bodyLarge,
                color = if (diversSpentCents > 0L && afterDiversCents < 0L) {
                    A11yTokens.DangerInk
                } else {
                    A11yTokens.InkMuted
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = statusLine }
            )
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text("Montant (€)") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(fieldBring),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            // P1-H4 : 1 Primary carnet + QuietLink corriger (pas 2 Secondary outils).
            PrimaryA11yButton(
                text = "J’ai dépensé",
                onClick = onAdd,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            )
            QuietLinkA11y(
                text = "Corriger",
                onClick = onRemove,
                enabled = !busy,
                fillMaxWidth = false,
                modifier = Modifier.fillMaxWidth(),
                onClickLabel = "Corriger le montant des courses"
            )
            error?.let { err ->
                val errShape = MaterialTheme.shapes.medium
                Text(
                    text = err,
                    color = A11yTokens.DangerInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.DangerInk.copy(alpha = 0.4f), errShape)
                        .background(A11yTokens.PaperWarm, errShape)
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Assertive
                            contentDescription = err
                        }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
