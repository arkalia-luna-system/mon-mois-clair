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
 * \u00c9dition rapide du forfait \u00ab Mettre de c\u00f4t\u00e9 \u00bb depuis le sticky Accueil.
 * Jour / retrait / historique : R\u00e9glages (lien discret).
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
            CarnetPageTitle("Mettre de c\u00f4t\u00e9", withTopSpacer = false, shortSheet = true)
            Text(
                text = if (active) {
                    "Not\u00e9 ${MoneyFormat.formatCents(monthlyCents)} / mois \u00b7 d\u00e9j\u00e0 retir\u00e9 du reste"
                } else {
                    "Vers un autre compte \u2014 ce montant est retir\u00e9 du reste du mois."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HomeTokens.chipRadius)
                    .background(HomeTokens.SavingsChip)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .semantics {
                        contentDescription = if (active) {
                            "Not\u00e9 ${MoneyFormat.formatCents(monthlyCents)} par mois"
                        } else {
                            "Mettre de c\u00f4t\u00e9 vers un autre compte"
                        }
                    }
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
            // Parit\u00e9 sticky Courses : CTA primaire teal (QuietLink = d\u00e9tail).
            PrimaryA11yButton(
                text = if (active) "Mettre \u00e0 jour" else "Activer",
                onClick = onSave,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            )
            QuietLinkA11y(
                text = "Jour et d\u00e9tail \u203a",
                onClick = {
                    onDismiss()
                    onOpenDetails()
                },
                onClickLabel = "Ouvrir le d\u00e9tail dans R\u00e9glages"
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
