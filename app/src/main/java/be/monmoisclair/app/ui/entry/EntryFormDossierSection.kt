package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.settings.PracticalContextTips
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors

/** Section Plan de remboursement (dossier BE). */
@Composable
internal fun EntryFormDossierSection(
    showDossierSection: Boolean,
    dossierExpanded: Boolean,
    onDossierExpanded: (Boolean) -> Unit,
    isAgreement: Boolean,
    causeLabel: String,
    onCauseLabel: (String) -> Unit,
    causeBring: Modifier,
    openingRemainingText: String,
    onOpeningRemainingText: (String) -> Unit,
    remainingBring: Modifier,
    primaryEnabled: Boolean,
    onSaveAsPendingLetter: (() -> Unit)?,
    iban: String,
    onIban: (String) -> Unit,
    onCopyIban: (() -> Unit)?,
    creditorEmail: String,
    onCreditorEmail: (String) -> Unit,
    onWriteMail: (() -> Unit)?,
    feeAddText: String,
    onFeeAddText: (String) -> Unit,
    feeNoteText: String,
    onFeeNoteText: (String) -> Unit,
    addedFeesCents: Long,
    feeEvents: List<FeeEventRow>,
    focusManager: FocusManager,
    softWarns: List<String> = emptyList()
) {
    if (showDossierSection) {
        // FORM04 / FORM01 : Accord après Paiement — filet + repli.
        HorizontalDivider(color = A11yTokens.PaperBorder)
        FormSectionToggle(
            title = EntryFormMessages.DOSSIER_SECTION_TITLE,
            expanded = dossierExpanded,
            closedHint = "Accord de paiement — pas la pastille En retard",
            onToggle = { onDossierExpanded(!dossierExpanded) }
        )
        if (dossierExpanded) {
            // Accordion : champs ; reste/rythme déjà au-dessus de la grille si accord.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            if (!isAgreement) {
                Text(
                    text = "Accord de paiement (reste dû). " +
                        "Pas la pastille « En retard » de l’accueil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            softWarns.forEach { warn ->
                Text(
                    text = warn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.AccentWarn,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = causeLabel,
                onValueChange = onCauseLabel,
                label = { Text("Pour quoi (ex. Hôpital 2025)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(causeBring),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
            )
            if (!isAgreement) {
                OutlinedTextField(
                    value = openingRemainingText,
                    onValueChange = onOpeningRemainingText,
                    label = { Text("Combien reste-t-il aujourd’hui ?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .then(remainingBring),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = carnetOutlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text(
                    text = "Vide = pas d’accord. En haut = le versement de ce mois.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onSaveAsPendingLetter != null &&
                    openingRemainingText.isBlank()
                ) {
                    QuietLinkA11y(
                        text = "Garder le courrier (sans montant)",
                        onClick = onSaveAsPendingLetter,
                        enabled = primaryEnabled
                    )
                }
            }
            // Lot C — IBAN / e-mail / frais (dossier seulement).
            OutlinedTextField(
                value = iban,
                onValueChange = onIban,
                label = { Text("IBAN") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            if (onCopyIban != null && iban.isNotBlank()) {
                CarnetTipCard(text = PracticalContextTips.CLIPBOARD_IBAN)
                QuietLinkA11y(text = "Copier l’IBAN", onClick = onCopyIban)
            }
            OutlinedTextField(
                value = creditorEmail,
                onValueChange = onCreditorEmail,
                label = { Text("E-mail (pour Écrire)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            if (onWriteMail != null && creditorEmail.isNotBlank()) {
                QuietLinkA11y(text = "Écrire", onClick = onWriteMail)
            }
            if (isAgreement) {
                if (feeEvents.isNotEmpty() || addedFeesCents > 0L) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Frais notés : ${MoneyFormat.formatCents(addedFeesCents)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.Ink
                        )
                        feeEvents.forEach { ev ->
                            val line = buildString {
                                append(ev.dateLabel)
                                append(" · ")
                                append(MoneyFormat.formatCents(ev.amountCents))
                                if (ev.note.isNotBlank()) {
                                    append(" — ")
                                    append(ev.note)
                                }
                            }
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = feeAddText,
                    onValueChange = onFeeAddText,
                    label = { Text("Ils ont ajouté un montant (€)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = carnetOutlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = feeNoteText,
                    onValueChange = onFeeNoteText,
                    label = { Text("Motif (ex. frais de rappel)") },
                    supportingText = {
                        Text("Chaque montant s’ajoute à la liste avec la date du jour.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = carnetOutlinedTextFieldColors(),
                )
            }
            } // fin carte dossier
        }
    }
}
