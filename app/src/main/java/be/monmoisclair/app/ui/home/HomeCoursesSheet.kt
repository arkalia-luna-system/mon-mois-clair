package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSheetHandle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.HomeTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance

/**
 * Unique UI d'édition Courses / divers — ouverte depuis la bannière sticky.
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
        containerColor = HomeTokens.Canvas,
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
                    "Not\u00e9 ${MoneyFormat.formatCents(diversSpentCents)} \u00b7 il reste " +
                        "${MoneyFormat.formatCents(afterDiversCents)}"
                diversSpentCents > 0L ->
                    "Not\u00e9 ${MoneyFormat.formatCents(diversSpentCents)} \u00b7 hors du reste du mois"
                else -> "Hors du reste du mois"
            }
            Text(
                text = statusLine,
                style = MaterialTheme.typography.bodyMedium,
                color = if (diversSpentCents > 0L && afterDiversCents < 0L) {
                    A11yTokens.DangerInk
                } else {
                    A11yTokens.InkMuted
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HomeTokens.chipRadius)
                    .background(
                        if (diversSpentCents > 0L && afterDiversCents < 0L) {
                            A11yTokens.PaperWarm
                        } else {
                            HomeTokens.CoursesChip
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .semantics { contentDescription = statusLine }
            )
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text("Montant (\u20ac)") },
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
            error?.let { err ->
                CarnetStateBanner(text = err, kind = CarnetBannerKind.Error, pad = 12.dp)
            }
            // P1-H4 : 1 Primary carnet + QuietLink retirer (pas 2 Secondary outils).
            PrimaryA11yButton(
                text = "J\u2019ai d\u00e9pens\u00e9",
                onClick = onAdd,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            )
            QuietLinkA11y(
                text = "Retirer",
                onClick = onRemove,
                enabled = !busy,
                fillMaxWidth = false,
                modifier = Modifier.fillMaxWidth(),
                onClickLabel = "Retirer un montant des courses"
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
