package be.monmoisclair.app.ui.entry

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.CreditorMailComposer
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import java.io.File

@Composable
internal fun DiscardChangesDialog(
    onStay: () -> Unit,
    onLeave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStay,
        shape = MaterialTheme.shapes.medium,
        containerColor = A11yTokens.PaperCream,
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
                onClick = onLeave,
                fillMaxWidth = false,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        },
        dismissButton = {
            SecondaryA11yButton(
                text = "Rester",
                onClick = onStay,
                fillMaxWidth = false,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    )
}

@Composable
internal fun EntryFormScaffold(
    title: String,
    label: String,
    amountText: String,
    dayText: String,
    isExpense: Boolean,
    recurring: Boolean,
    showRecurring: Boolean,
    recurrence: RecurrenceChoice = RecurrenceChoice.Once,
    onRecurrence: ((RecurrenceChoice) -> Unit)? = null,
    showFrequencyEdit: Boolean = false,
    recurrenceInfo: String? = null,
    receiptPreview: Bitmap? = null,
    nextDueMonth: Int = 1,
    onNextDueMonth: (Int) -> Unit = {},
    showNextDueMonth: Boolean = false,
    isAutoDebit: Boolean = false,
    onAutoDebit: (Boolean) -> Unit = {},
    showAutoDebit: Boolean = false,
    paymentNote: String = "",
    onPaymentNote: (String) -> Unit = {},
    showPaymentAssist: Boolean = false,
    hasPendingPhoto: Boolean = false,
    hasPendingPdf: Boolean = false,
    hasReceiptPhoto: Boolean = false,
    receiptIsPdf: Boolean = false,
    onScanEpc: (() -> Unit)? = null,
    onTakePhoto: (() -> Unit)? = null,
    onPickPdf: (() -> Unit)? = null,
    onClearPendingPdf: (() -> Unit)? = null,
    onClearReceipt: (() -> Unit)? = null,
    onViewReceipt: (() -> Unit)? = null,
    /** Courriers en plus (dossier) — chemins relatifs déjà parsés. */
    extraReceiptPaths: List<String> = emptyList(),
    isPdfPath: (String) -> Boolean = { false },
    onViewExtraReceipt: ((String) -> Unit)? = null,
    onRemoveExtraReceipt: ((String) -> Unit)? = null,
    onCopyPaymentNote: (() -> Unit)? = null,
    /** OCR +++ sur la photo — jamais le montant. */
    onReadCommFromPhoto: (() -> Unit)? = null,
    ocrBusy: Boolean = false,
    showApplyFollowing: Boolean,
    applyFollowing: Boolean,
    hasTemplate: Boolean,
    showDossierSection: Boolean = false,
    dossierExpanded: Boolean = false,
    onDossierExpanded: (Boolean) -> Unit = {},
    causeLabel: String = "",
    onCauseLabel: (String) -> Unit = {},
    openingRemainingText: String = "",
    onOpeningRemainingText: (String) -> Unit = {},
    isAgreement: Boolean = false,
    remainingCents: Long? = null,
    isSettled: Boolean = false,
    pacePhrase: String? = null,
    isSavingsLine: Boolean = false,
    savingsTransferredCents: Long = 0L,
    lockAmountAndDay: Boolean = false,
    iban: String = "",
    onIban: (String) -> Unit = {},
    onCopyIban: (() -> Unit)? = null,
    creditorEmail: String = "",
    onCreditorEmail: (String) -> Unit = {},
    onWriteMail: (() -> Unit)? = null,
    /** Lot D — ajout seulement : ranger sans inventer de montant. */
    onSaveAsPendingLetter: (() -> Unit)? = null,
    feeAddText: String = "",
    onFeeAddText: (String) -> Unit = {},
    feeNoteText: String = "",
    onFeeNoteText: (String) -> Unit = {},
    addedFeesCents: Long = 0L,
    feeEvents: List<FeeEventRow> = emptyList(),
    infoMessage: String? = null,
    error: String?,
    primaryLabel: String,
    primaryEnabled: Boolean,
    deleteLabel: String = "Supprimer",
    onLabel: (String) -> Unit,
    onAmount: (String) -> Unit,
    onDay: (String) -> Unit,
    onExpense: (Boolean) -> Unit,
    onRecurring: (Boolean) -> Unit,
    onApplyFollowing: (Boolean) -> Unit,
    onPrimary: () -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?
) {
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
    val formGap = if (fit.compactHomeChrome) 10.dp else 14.dp
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
            // Sous sticky Enregistrer / Annuler / Supprimer : jour 31 scrollable
            // (3×48 dp + clearance — sticky 3 actions plus haut que 2× sur S938B).
            Spacer(
                modifier = Modifier.height(
                    A11yTokens.minTouchHeight * 3 + A11yTokens.bottomSystemClearance
                )
            )

            // Édition ponctuelle (sans modèle) : info seule.
            if (!showRecurring && !showFrequencyEdit) {
                recurrenceInfo?.let { info ->
                    Text(text = "Fréquence", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
                    Text(text = info, style = MaterialTheme.typography.bodyLarge)
                }
            }

            if ((showRecurring || showFrequencyEdit) && onRecurrence != null) {
                CarnetSectionTitle("Fréquence")
                if (showFrequencyEdit) {
                    Text(
                        text = "Mois déjà créés : cochez « Aussi les mois suivants ».",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeChip(RecurrenceLabel.chipOnce(), selected = recurrence == RecurrenceChoice.Once) {
                        onRecurrence(RecurrenceChoice.Once)
                    }
                    TypeChip(RecurrenceLabel.chipMonthly(), selected = recurrence == RecurrenceChoice.Monthly) {
                        onRecurrence(RecurrenceChoice.Monthly)
                    }
                    TypeChip(
                        RecurrenceLabel.chipEvery3Months(),
                        selected = recurrence == RecurrenceChoice.Every3Months
                    ) {
                        onRecurrence(RecurrenceChoice.Every3Months)
                    }
                    TypeChip(RecurrenceLabel.chipYearly(), selected = recurrence == RecurrenceChoice.Yearly) {
                        onRecurrence(RecurrenceChoice.Yearly)
                    }
                }
                if (showNextDueMonth) {
                    CarnetSectionTitle("Prochaine échéance")
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (m in 1..12) {
                            TypeChip(
                                text = be.monmoisclair.app.domain.RecurrenceAnchor.frenchMonthLabel(m),
                                selected = nextDueMonth == m
                            ) {
                                onNextDueMonth(m)
                            }
                        }
                    }
                }
            } else if (showRecurring) {
                CheckRow(
                    checked = recurring,
                    label = RecurrenceLabel.forIntervalMonths(1),
                    onChecked = onRecurring
                )
            }
            // FORM01 : qui → combien → jour (ci-dessus) → Paiement & preuve → Accord.
            // Prélèvement vit dans Paiement (pas un tip cockpit entre grille et section).
            if (showPaymentAssist) {
                val hasPaymentContent =
                    paymentNote.isNotBlank() ||
                        hasReceiptPhoto ||
                        hasPendingPhoto ||
                        hasPendingPdf ||
                        extraReceiptPaths.isNotEmpty() ||
                        isAutoDebit
                // Fermé par défaut : grille jours reste au premier plan ; pièce en attente → ouvre.
                var paymentExpanded by remember {
                    mutableStateOf(hasPendingPhoto || hasPendingPdf)
                }
                LaunchedEffect(hasPendingPhoto, hasPendingPdf) {
                    if (hasPendingPhoto || hasPendingPdf) paymentExpanded = true
                }
                HorizontalDivider(color = A11yTokens.PaperBorder)
                FormSectionToggle(
                    title = "Paiement & preuve",
                    expanded = paymentExpanded,
                    closedHint = when {
                        hasPaymentContent -> "Noté — touchez pour voir"
                        else -> "QR, +++ , photo, PDF"
                    },
                    onToggle = { paymentExpanded = !paymentExpanded }
                )
                if (paymentExpanded) {
                    // Secondary = QR / photo / PDF seulement ;
                    // QuietLink = Copier / Voir / Enlever / OCR (2ᵉ rang).
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (showAutoDebit) {
                            CheckRow(
                                checked = isAutoDebit,
                                label = "Prélèvement automatique",
                                onChecked = onAutoDebit
                            )
                        }
                        if (onScanEpc != null) {
                            SecondaryA11yButton(text = "Scanner une facture (QR)", onClick = onScanEpc)
                        }
                        OutlinedTextField(
                            value = paymentNote,
                            onValueChange = onPaymentNote,
                            label = { Text("Référence / communication") },
                            placeholder = { Text("+++090/9337/55493+++") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .then(noteBring),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            shape = MaterialTheme.shapes.medium,
                            colors = carnetOutlinedTextFieldColors(),
                        )
                        if (!isAgreement) {
                            Text(
                                text = "Eau, énergie, télécom : souvent une nouvelle +++ chaque mois.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (onCopyPaymentNote != null && paymentNote.isNotBlank()) {
                            QuietLinkA11y(
                                text = "Copier la communication",
                                onClick = onCopyPaymentNote
                            )
                        }
                        if (onReadCommFromPhoto != null) {
                            QuietLinkA11y(
                                text = if (ocrBusy) {
                                    "Lecture de la photo…"
                                } else {
                                    "Lire la communication sur la photo"
                                },
                                onClick = onReadCommFromPhoto,
                                enabled = !ocrBusy
                            )
                        }
                        // Photo / PDF : CTA d’ajout seulement si absents ;
                        // sinon via le bloc pièce (aperçu / reprendre / enlever).
                        val noPhotoYet = !hasReceiptPhoto && !hasPendingPhoto
                        val noPdfYet = !hasReceiptPhoto && !hasPendingPdf &&
                            extraReceiptPaths.none { isPdfPath(it) }
                        val appendOnAgreement =
                            isAgreement && hasReceiptPhoto && !hasPendingPhoto && !hasPendingPdf
                        if (onTakePhoto != null && (noPhotoYet || appendOnAgreement)) {
                            SecondaryA11yButton(
                                text = if (appendOnAgreement) {
                                    "Ajouter un courrier (photo)"
                                } else {
                                    "Photo de la facture papier"
                                },
                                onClick = onTakePhoto
                            )
                        }
                        if (onPickPdf != null && (noPdfYet || appendOnAgreement ||
                                (hasReceiptPhoto && !receiptIsPdf && !hasPendingPdf))
                        ) {
                            // PDF absent, photo seule (joindre à côté), ou accord (ajouter).
                            SecondaryA11yButton(text = "Joindre un PDF", onClick = onPickPdf)
                        }
                        if (hasPendingPhoto) {
                            val photoOkShape = MaterialTheme.shapes.medium
                            val photoOkText = if (isAgreement && hasReceiptPhoto) {
                                "Photo prête — s’ajoute aux courriers à l’enregistrement."
                            } else {
                                "Photo prête — Enregistrer pour la garder."
                            }
                            Text(
                                text = photoOkText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.AccentOkInk,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                                        photoOkShape
                                    )
                                    .background(A11yTokens.AccentOkSoft, photoOkShape)
                                    .padding(10.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                        contentDescription = photoOkText
                                    }
                            )
                            if (onTakePhoto != null) {
                                QuietLinkA11y(
                                    text = "Reprendre la photo",
                                    onClick = onTakePhoto
                                )
                            }
                        }
                        if (hasPendingPdf) {
                            val pdfOkShape = MaterialTheme.shapes.medium
                            val pdfOkText = if (hasReceiptPhoto) {
                                "PDF prêt — s’ajoute à côté de la photo à l’enregistrement."
                            } else {
                                "PDF prêt — Enregistrer pour le garder."
                            }
                            Text(
                                text = pdfOkText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.AccentOkInk,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                                        pdfOkShape
                                    )
                                    .background(A11yTokens.AccentOkSoft, pdfOkShape)
                                    .padding(10.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                        contentDescription = pdfOkText
                                    }
                            )
                            if (onPickPdf != null) {
                                QuietLinkA11y(text = "Changer le PDF", onClick = onPickPdf)
                            }
                            if (onClearPendingPdf != null) {
                                QuietLinkA11y(
                                    text = "Enlever ce courrier",
                                    onClick = onClearPendingPdf
                                )
                            }
                        }
                        // Bloc pièce existante (aperçu / voir / reprendre / enlever).
                        if (
                            hasReceiptPhoto &&
                            receiptPreview == null &&
                            onViewReceipt != null &&
                            !hasPendingPhoto &&
                            !hasPendingPdf
                        ) {
                            QuietLinkA11y(
                                text = if (receiptIsPdf) {
                                    "Voir le courrier (PDF)"
                                } else {
                                    "Voir la photo de la facture"
                                },
                                onClick = onViewReceipt
                            )
                        }
                        receiptPreview?.let { bmp ->
                            // FORM06 : miniature seule — contentDescription / onClickLabel suffisent.
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Photo de la facture — touchez pour agrandir",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(A11yTokens.PaperWarm)
                                    .then(
                                        if (onViewReceipt != null) {
                                            Modifier.clickable(
                                                role = Role.Button,
                                                onClickLabel = "Agrandir la facture",
                                                onClick = onViewReceipt
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        }
                        if (hasReceiptPhoto && !hasPendingPhoto) {
                            if (onTakePhoto != null && !receiptIsPdf) {
                                QuietLinkA11y(
                                    text = "Reprendre la photo de la facture",
                                    onClick = onTakePhoto
                                )
                            }
                            if (onPickPdf != null && receiptIsPdf) {
                                QuietLinkA11y(
                                    text = "Remplacer le PDF",
                                    onClick = onPickPdf
                                )
                            }
                            if (onClearReceipt != null) {
                                QuietLinkA11y(
                                    text = if (
                                        !applyFollowing &&
                                        !isSavingsLine &&
                                        (isAgreement || hasTemplate)
                                    ) {
                                        if (receiptIsPdf) {
                                            "Enlever le PDF de ce mois"
                                        } else {
                                            "Enlever la photo de ce mois"
                                        }
                                    } else if (receiptIsPdf) {
                                        "Enlever le PDF"
                                    } else {
                                        "Enlever la photo"
                                    },
                                    onClick = onClearReceipt
                                )
                            }
                        }
                        if (extraReceiptPaths.isNotEmpty()) {
                            Text(
                                text = if (hasReceiptPhoto) "Autres courriers" else "Courriers",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.InkMuted
                            )
                            extraReceiptPaths.forEachIndexed { index, path ->
                                val n = if (hasReceiptPhoto) index + 2 else index + 1
                                val pdf = isPdfPath(path)
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (onViewExtraReceipt != null) {
                                        QuietLinkA11y(
                                            text = if (pdf) {
                                                "Voir le courrier $n (PDF)"
                                            } else {
                                                "Voir le courrier $n"
                                            },
                                            onClick = { onViewExtraReceipt(path) },
                                            fillMaxWidth = false
                                        )
                                    }
                                    if (onRemoveExtraReceipt != null) {
                                        QuietLinkA11y(
                                            text = "Enlever le courrier $n",
                                            onClick = { onRemoveExtraReceipt(path) },
                                            fillMaxWidth = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (showAutoDebit) {
                CheckRow(
                    checked = isAutoDebit,
                    label = "Prélèvement automatique",
                    onChecked = onAutoDebit
                )
            }

            if (showDossierSection) {
                // FORM04 / FORM01 : Accord après Paiement — filet + repli.
                HorizontalDivider(color = A11yTokens.PaperBorder)
                FormSectionToggle(
                    title = "Retard ou accord",
                    expanded = dossierExpanded,
                    closedHint = "Huissier, hôpital, plan de paiement…",
                    onToggle = { onDossierExpanded(!dossierExpanded) }
                )
                if (dossierExpanded) {
                    // Accordion : champs ; reste/rythme déjà au-dessus de la grille si accord.
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    if (!isAgreement) {
                        if (isSettled) {
                            Text(
                                text = "Soldé",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.AccentOkInk
                            )
                        }
                        remainingCents?.let { rem ->
                            if (!isSettled) {
                                Text(
                                    text = "Encore ${MoneyFormat.formatCents(rem)} à rembourser.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = A11yTokens.InkMuted,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                            text = "Vide = pas un accord. Le montant du haut = ce mois.",
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
            if (showApplyFollowing && hasTemplate) {
                CheckRow(
                    checked = applyFollowing,
                    label = "Aussi les mois suivants",
                    onChecked = onApplyFollowing
                )
                Text(
                    text = "Décoché = ce mois seulement (montant / photo).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            infoMessage?.let {
                val infoShape = MaterialTheme.shapes.medium
                val warn = it == BelgianStructuredCommunication.VERIFY_ON_MAIL
                Text(
                    text = it,
                    color = if (warn) A11yTokens.AccentWarn else A11yTokens.AccentOkInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (warn) A11yTokens.AccentWarn.copy(alpha = 0.4f)
                            else A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                            infoShape
                        )
                        .background(
                            if (warn) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            infoShape
                        )
                        .padding(tipPad)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = it
                        }
                )
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
            val errShape = MaterialTheme.shapes.medium
            Text(
                text = it,
                color = A11yTokens.DangerInk,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.DangerInk.copy(alpha = 0.4f), errShape)
                    .background(A11yTokens.PaperWarm, errShape)
                    .padding(tipPad)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = it
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        // FORM05 : socle papier (filet) ; 1 Primary ; Annuler = QuietLink (pas titleLarge).
        HorizontalDivider(color = A11yTokens.PaperBorder)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(A11yTokens.PaperWarm)
                .padding(top = 10.dp)
        ) {
            PrimaryA11yButton(text = primaryLabel, onClick = onPrimary, enabled = primaryEnabled)
            if (!keyboardOpen) {
                // C03 : QuietLink centré entre Primary et Danger — moins écrasé.
                Spacer(modifier = Modifier.height(6.dp))
                QuietLinkA11y(
                    text = "Annuler",
                    onClick = onCancel,
                    enabled = primaryEnabled,
                    fillMaxWidth = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClickLabel = "Annuler sans enregistrer"
                )
                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    DangerA11yButton(
                        text = deleteLabel,
                        onClick = onDelete,
                        enabled = primaryEnabled
                    )
                }
            }
        }
    }
}
