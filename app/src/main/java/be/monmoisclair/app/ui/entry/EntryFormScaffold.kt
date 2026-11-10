package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.DiscardChangesDialog
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
internal fun DuplicateExpenseDialog(
    body: String,
    onCreateAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = A11yTokens.PaperCard,
        title = {
            Text(
                text = "Une ligne ressemblante existe déjà",
                color = A11yTokens.Ink
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.Ink
            )
        },
        confirmButton = {
            PrimaryA11yButton(
                text = "Créer quand même",
                onClick = onCreateAnyway,
                fillMaxWidth = false,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        },
        dismissButton = {
            SecondaryA11yButton(
                text = "Revenir modifier",
                onClick = onDismiss,
                fillMaxWidth = false,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    )
}

@Composable
internal fun EntryFormScaffold(
    identity: EntryFormIdentityUi,
    recurrenceUi: EntryFormRecurrenceUi,
    payment: EntryFormPaymentUi,
    dossier: EntryFormDossierUi,
    savings: EntryFormSavingsUi = EntryFormSavingsUi(),
    chrome: EntryFormChromeUi
) {
    val title = identity.title
    val label = identity.label
    val amountText = identity.amountText
    val dayText = identity.dayText
    val isExpense = identity.isExpense
    val onLabel = identity.onLabel
    val onAmount = identity.onAmount
    val onDay = identity.onDay
    val onExpense = identity.onExpense

    val recurring = recurrenceUi.recurring
    val showRecurring = recurrenceUi.showRecurring
    val recurrence = recurrenceUi.recurrence
    val onRecurrence = recurrenceUi.onRecurrence
    val showFrequencyEdit = recurrenceUi.showFrequencyEdit
    val recurrenceInfo = recurrenceUi.recurrenceInfo
    val nextDueMonth = recurrenceUi.nextDueMonth
    val onNextDueMonth = recurrenceUi.onNextDueMonth
    val showNextDueMonth = recurrenceUi.showNextDueMonth
    val onRecurring = recurrenceUi.onRecurring
    val showApplyFollowing = recurrenceUi.showApplyFollowing
    val applyFollowing = recurrenceUi.applyFollowing
    val hasTemplate = recurrenceUi.hasTemplate
    val onApplyFollowing = recurrenceUi.onApplyFollowing

    val isAutoDebit = payment.isAutoDebit
    val onAutoDebit = payment.onAutoDebit
    val showAutoDebit = payment.showAutoDebit
    val paymentNote = payment.paymentNote
    val onPaymentNote = payment.onPaymentNote
    val showPaymentAssist = payment.showPaymentAssist
    val hasPendingPhoto = payment.hasPendingPhoto
    val hasPendingPdf = payment.hasPendingPdf
    val hasReceiptPhoto = payment.hasReceiptPhoto
    val receiptIsPdf = payment.receiptIsPdf
    val receiptPreview = payment.receiptPreview
    val extraReceiptPaths = payment.extraReceiptPaths
    val isPdfPath = payment.isPdfPath
    val ocrBusy = payment.ocrBusy
    val onScanEpc = payment.onScanEpc
    val onTakePhoto = payment.onTakePhoto
    val onPickPdf = payment.onPickPdf
    val onClearPendingPdf = payment.onClearPendingPdf
    val onClearReceipt = payment.onClearReceipt
    val onViewReceipt = payment.onViewReceipt
    val onViewExtraReceipt = payment.onViewExtraReceipt
    val onRemoveExtraReceipt = payment.onRemoveExtraReceipt
    val onCopyPaymentNote = payment.onCopyPaymentNote
    val onReadCommFromPhoto = payment.onReadCommFromPhoto

    val showDossierSection = dossier.showDossierSection
    val dossierExpanded = dossier.dossierExpanded
    val onDossierExpanded = dossier.onDossierExpanded
    val causeLabel = dossier.causeLabel
    val onCauseLabel = dossier.onCauseLabel
    val openingRemainingText = dossier.openingRemainingText
    val onOpeningRemainingText = dossier.onOpeningRemainingText
    val isAgreement = dossier.isAgreement
    val remainingCents = dossier.remainingCents
    val isSettled = dossier.isSettled
    val pacePhrase = dossier.pacePhrase
    val iban = dossier.iban
    val onIban = dossier.onIban
    val onCopyIban = dossier.onCopyIban
    val creditorEmail = dossier.creditorEmail
    val onCreditorEmail = dossier.onCreditorEmail
    val onWriteMail = dossier.onWriteMail
    val onSaveAsPendingLetter = dossier.onSaveAsPendingLetter
    val feeAddText = dossier.feeAddText
    val onFeeAddText = dossier.onFeeAddText
    val feeNoteText = dossier.feeNoteText
    val onFeeNoteText = dossier.onFeeNoteText
    val addedFeesCents = dossier.addedFeesCents
    val feeEvents = dossier.feeEvents

    val isSavingsLine = savings.isSavingsLine
    val savingsTransferredCents = savings.savingsTransferredCents
    val lockAmountAndDay = savings.lockAmountAndDay

    val infoMessage = chrome.infoMessage
    val error = chrome.error
    val primaryLabel = chrome.primaryLabel
    val primaryEnabled = chrome.primaryEnabled
    val deleteLabel = chrome.deleteLabel
    val onPrimary = chrome.onPrimary
    val onCancel = chrome.onCancel
    val onDelete = chrome.onDelete

    val focusManager = LocalFocusManager.current
    val keyboardOpen = imeOpen()
    val nameBring = rememberFocusKeepsVisible()
    val amountBring = rememberFocusKeepsVisible()
    val noteBring = rememberFocusKeepsVisible()
    val causeBring = rememberFocusKeepsVisible()
    val remainingBring = rememberFocusKeepsVisible()
    val nameFieldLabel = if (showDossierSection && dossierExpanded) {
        "À qui (ex. Étude Leroy)"
    } else {
        "Nom (ex. Loyer)"
    }
    val fit = rememberDisplayFit()
    // P1-F1 : plus d’air entre sections fiche.
    val formGap = if (fit.compactHomeChrome) 14.dp else 18.dp
    val tipPad = fit.tipPad
    // Champs scrollables. Au clavier : un seul bouton, le champ actif reste visible.
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
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(formGap)
        ) {
            CarnetPageTitle(title, withTopSpacer = false)

            if (showRecurring) {
                val chipGap = if (fit.compactHomeChrome) 6.dp else 8.dp
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(chipGap),
                    verticalArrangement = Arrangement.spacedBy(chipGap)
                ) {
                    TypeChip("Dépense (−)", selected = isExpense) { onExpense(true) }
                    TypeChip("Revenu (+)", selected = !isExpense) { onExpense(false) }
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = onLabel,
                label = { Text(nameFieldLabel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(nameBring),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                readOnly = isSavingsLine,
                enabled = !isSavingsLine,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = onAmount,
                label = { Text("Montant (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(amountBring),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                enabled = !lockAmountAndDay
            )
            if (isSavingsLine) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Pas d’argent gardé dans l’app — virement vers un autre compte.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                    if (savingsTransferredCents > 0L) {
                        Text(
                            text = "Déjà viré : ${MoneyFormat.formatCents(savingsTransferredCents)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                    }
                    if (lockAmountAndDay) {
                        Text(
                            text = "FAIT ce mois. Suite : Réglages → Mettre de côté.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                    }
                }
            }
            // Accords : reste + rythme au-dessus de la grille (campagne D2 / D11 —
            // ne plus exiger de scroller sous les 31 jours).
            if (isAgreement && !isSavingsLine) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSettled) {
                        Text(
                            text = "Soldé",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.AccentOkInk
                        )
                    } else {
                        remainingCents?.let { rem ->
                            Text(
                                text = "Encore ${MoneyFormat.formatCents(rem)} à rembourser.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.Ink
                            )
                        }
                        val interval = when (recurrence) {
                            RecurrenceChoice.Every3Months -> 3
                            RecurrenceChoice.Yearly -> 12
                            else -> 1
                        }
                        val forfait = MoneyFormat.parseToCents(amountText)
                        if (forfait != null && forfait > 0L && !isSettled) {
                            Text(
                                text = MoneyFormat.formatCents(forfait) + " " +
                                    be.monmoisclair.app.domain.DossierRemaining.cadenceSuffix(interval),
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted
                            )
                        }
                        if (!pacePhrase.isNullOrBlank()) {
                            Text(
                                text = pacePhrase,
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted
                            )
                        }
                    }
                }
            }
            DayOfMonthGrid(
                selectedDay = dayText.trim().toIntOrNull()?.takeIf { it in 1..31 },
                onSelect = { onDay(it.toString()) },
                enabled = !lockAmountAndDay
            )
            // 7.07 : plus de spacer mort ~172 dp entre grille et Fréquence
            // (clearance sticky = Spacer bas unique, plus bas).

            EntryFormRecurrenceSection(
                showRecurring = showRecurring,
                showFrequencyEdit = showFrequencyEdit,
                recurrence = recurrence,
                onRecurrence = onRecurrence,
                recurrenceInfo = recurrenceInfo,
                nextDueMonth = nextDueMonth,
                onNextDueMonth = onNextDueMonth,
                showNextDueMonth = showNextDueMonth,
                recurring = recurring,
                onRecurring = onRecurring
            )
            EntryFormPaymentSection(
                showPaymentAssist = showPaymentAssist,
                showAutoDebit = showAutoDebit,
                isAutoDebit = isAutoDebit,
                onAutoDebit = onAutoDebit,
                paymentNote = paymentNote,
                onPaymentNote = onPaymentNote,
                noteBring = noteBring,
                isAgreement = isAgreement,
                hasPendingPhoto = hasPendingPhoto,
                hasPendingPdf = hasPendingPdf,
                hasReceiptPhoto = hasReceiptPhoto,
                receiptIsPdf = receiptIsPdf,
                receiptPreview = receiptPreview,
                extraReceiptPaths = extraReceiptPaths,
                isPdfPath = isPdfPath,
                applyFollowing = applyFollowing,
                isSavingsLine = isSavingsLine,
                hasTemplate = hasTemplate,
                ocrBusy = ocrBusy,
                onScanEpc = onScanEpc,
                onTakePhoto = onTakePhoto,
                onPickPdf = onPickPdf,
                onClearPendingPdf = onClearPendingPdf,
                onClearReceipt = onClearReceipt,
                onViewReceipt = onViewReceipt,
                onViewExtraReceipt = onViewExtraReceipt,
                onRemoveExtraReceipt = onRemoveExtraReceipt,
                onCopyPaymentNote = onCopyPaymentNote,
                onReadCommFromPhoto = onReadCommFromPhoto
            )
            EntryFormDossierSection(
                showDossierSection = showDossierSection,
                dossierExpanded = dossierExpanded,
                onDossierExpanded = onDossierExpanded,
                isAgreement = isAgreement,
                causeLabel = causeLabel,
                onCauseLabel = onCauseLabel,
                causeBring = causeBring,
                openingRemainingText = openingRemainingText,
                onOpeningRemainingText = onOpeningRemainingText,
                remainingBring = remainingBring,
                primaryEnabled = primaryEnabled,
                onSaveAsPendingLetter = onSaveAsPendingLetter,
                iban = iban,
                onIban = onIban,
                onCopyIban = onCopyIban,
                creditorEmail = creditorEmail,
                onCreditorEmail = onCreditorEmail,
                onWriteMail = onWriteMail,
                feeAddText = feeAddText,
                onFeeAddText = onFeeAddText,
                feeNoteText = feeNoteText,
                onFeeNoteText = onFeeNoteText,
                addedFeesCents = addedFeesCents,
                feeEvents = feeEvents,
                focusManager = focusManager,
                softWarns = be.monmoisclair.app.domain.DossierSoftWarn.messages(
                    isAgreementOrOpening = isAgreement || openingRemainingText.isNotBlank(),
                    causeLabel = causeLabel,
                    hasPhoto = hasReceiptPhoto || hasPendingPhoto || hasPendingPdf,
                    iban = iban,
                    creditorEmail = creditorEmail
                )
            )
            if (showApplyFollowing && hasTemplate) {
                recurrenceInfo?.let { info ->
                    Text(
                        text = "Cette facture : $info",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.AccentOkInk,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                CheckRow(
                    checked = applyFollowing,
                    label = "Appliquer aussi aux mois suivants",
                    onChecked = onApplyFollowing
                )
                Text(
                    text = "À cocher seulement pour changer le montant, la photo ou le jour " +
                        "sur les prochains mois. La case repart décochée à chaque ouverture : " +
                        "ce n’est pas un réglage enregistré.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            infoMessage?.let {
                val infoShape = MaterialTheme.shapes.medium
                val warn = it == BelgianStructuredCommunication.VERIFY_ON_MAIL
                // P1-F1 : warn = boîte ; ok/tip courant = texte muted + filet (pas tip-box).
                if (warn) {
                    Text(
                        text = it,
                        color = A11yTokens.AccentWarn,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                A11yTokens.AccentWarn.copy(alpha = 0.4f),
                                infoShape
                            )
                            .background(A11yTokens.PaperWarm, infoShape)
                            .padding(tipPad)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = it
                            }
                    )
                } else {
                    Text(
                        text = it,
                        color = A11yTokens.AccentOkInk,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, A11yTokens.PaperBorder, infoShape)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = it
                            }
                    )
                }
            }
            // Marge basse : Primary + Annuler (+ Delete) + clearance — dernier QuietLink hors sticky.
            Spacer(
                modifier = Modifier.height(
                    A11yTokens.minTouchHeight * (if (onDelete != null) 3 else 2) +
                        A11yTokens.bottomSystemClearance
                )
            )
        }

        error?.let {
            CarnetStateBanner(text = it, kind = CarnetBannerKind.Error, pad = tipPad)
            Spacer(modifier = Modifier.height(8.dp))
        }
        // FORM05 7.11 : socle carte soft (ombre) ; 1 Primary ; Annuler = QuietLink.
        HorizontalDivider(color = A11yTokens.PaperBorder)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(A11yTokens.PaperCard)
                .padding(top = 12.dp)
        ) {
            PrimaryA11yButton(text = primaryLabel, onClick = onPrimary, enabled = primaryEnabled)
            if (!keyboardOpen) {
                // C03 : QuietLink centré entre Primary et Danger — moins écrasé.
                Spacer(modifier = Modifier.height(6.dp))
                QuietLinkA11y(
                    text = "Annuler",
                    onClick = onCancel,
                    // Toujours quittable (même si Enregistrer désactivé / lecture).
                    enabled = true,
                    fillMaxWidth = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClickLabel = "Annuler sans enregistrer"
                )
                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DangerA11yButton(
                        text = deleteLabel,
                        onClick = onDelete,
                        enabled = primaryEnabled,
                        onClickLabel = deleteLabel
                    )
                }
            }
        }
    }
}
