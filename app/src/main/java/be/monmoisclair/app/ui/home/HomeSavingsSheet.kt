package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance

/**
 * Édition rapide du forfait « Mettre de côté » depuis le sticky Accueil.
 * Jour / retrait / historique : Réglages (lien discret).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSavingsSheet(
    monthlyCents: Long,
    input: String,
    onInputChange: (String) -> Unit,
    error: String?,
    busy: Boolean,
    onSave: () -> Unit,
    onOpenDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fit = rememberDisplayFit()
    val sheetBottom = rememberSheetBottomClearance()
    val fieldBring = rememberFocusKeepsVisible()
    val gap = if (fit.compactHomeChrome) 8.dp else 12.dp
    val active = monthlyCents > 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
            CarnetPageTitle("Mettre de côté", withTopSpacer = false)
            Text(
                text = if (active) {
                    "Noté ${MoneyFormat.formatCents(monthlyCents)} / mois · dans le Reste"
                } else {
                    "Vers un autre compte — compte dans le Reste."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = if (active) {
                            "Noté ${MoneyFormat.formatCents(monthlyCents)} par mois"
                        } else {
                            "Mettre de côté vers un autre compte"
                        }
                    }
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
            SecondaryA11yButton(
                text = if (active) "Mettre à jour" else "Activer",
                onClick = onSave,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            )
            QuietLinkA11y(
                text = "Jour et détail ›",
                onClick = {
                    onDismiss()
                    onOpenDetails()
                },
                onClickLabel = "Ouvrir le détail dans Réglages"
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
