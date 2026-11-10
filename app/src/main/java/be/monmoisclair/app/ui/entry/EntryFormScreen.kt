package be.monmoisclair.app.ui.entry

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.DiscardChangesDialog
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.domain.CreditorMailComposer
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun AddEntryScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onScanEpc: () -> Unit = {},
    epcRawResult: String? = null,
    onEpcRawConsumed: () -> Unit = {},
    viewModel: AddEntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingPreview by remember { mutableStateOf<Bitmap?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }
    val takePhotoRaw = rememberFullPhotoCapture(
        onBitmap = { bmp ->
            pendingPreview = bmp
            viewModel.attachPreviewBitmap(bmp)
        },
        onDecodeFailed = {
            viewModel.setPhotoDecodeError()
        },
        onCaptureCancelled = {
            viewModel.setCameraCaptureError(EntryFormMessages.CAMERA_CAPTURE_CANCELLED)
        }
    )
    val takePhoto = be.monmoisclair.app.ui.camera.rememberCameraGatedLaunch(
        onGranted = takePhotoRaw,
        onMessage = viewModel::setCameraCaptureError,
        onOpenSettings = {
            if (!be.monmoisclair.app.ui.camera.openAppDetailsSettings(context)) {
                viewModel.setCameraCaptureError(
                    be.monmoisclair.app.ui.camera.CameraCaptureMessages.SETTINGS_OPEN_FAILED
                )
            }
        }
    )
    val pickPdf = rememberPdfPicker(
        onBytes = { bytes ->
            viewModel.attachPendingPdf(bytes)
        },
        onFailed = viewModel::setPdfReadError
    )
    var showReceiptDialog by remember { mutableStateOf(false) }
    // Déplier « plan de remboursement » sans rien saisir ≠ sale (C6 / Senior :
    // curiosité puis Annuler ne doit pas ouvrir « quitter sans sauver »).
    val dirty = state.label.isNotBlank() ||
        state.amountText.isNotBlank() ||
        state.paymentNote.isNotBlank() ||
        state.hasPendingPhoto ||
        state.hasPendingPdf ||
        pendingPreview != null ||
        state.recurrence != RecurrenceChoice.Once ||
        state.isAutoDebit ||
        state.causeLabel.isNotBlank() ||
        state.openingRemainingText.isNotBlank() ||
        state.iban.isNotBlank() ||
        state.creditorEmail.isNotBlank() ||
        !state.isExpense ||
        state.dayText != be.monmoisclair.app.domain.AppClock.today().dayOfMonth.toString()

    fun requestLeave() {
        if (dirty) confirmLeave = true else onCancel()
    }

    BackHandler { requestLeave() }
    if (confirmLeave) {
        DiscardChangesDialog(
            onStay = { confirmLeave = false },
            onLeave = {
                confirmLeave = false
                onCancel()
            }
        )
    }

    state.duplicateConfirm?.let { dup ->
        DuplicateExpenseDialog(
            body = be.monmoisclair.app.domain.SimilarExpenseMatcher.confirmDialogBody(dup),
            onCreateAnyway = viewModel::confirmDuplicateAndSave,
            onDismiss = viewModel::dismissDuplicateConfirm
        )
    }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    // Revenu : plus de photo/PDF — vider aussi l’aperçu Compose local (le VM a déjà clear).
    LaunchedEffect(state.isExpense, state.hasPendingPhoto) {
        if (!state.isExpense || !state.hasPendingPhoto) pendingPreview = null
    }
    LaunchedEffect(epcRawResult) {
        val raw = epcRawResult ?: return@LaunchedEffect
        viewModel.applyEpcRaw(raw)
        onEpcRawConsumed()
    }

    if (showReceiptDialog && pendingPreview != null) {
        Dialog(
            onDismissRequest = { showReceiptDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = A11yTokens.PaperCream,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(A11yTokens.screenPadding)
            ) {
                val dlgFit = rememberDisplayFit()
                val dlgPad = if (dlgFit.compactHomeChrome) 12.dp else 16.dp
                Column(
                    modifier = Modifier.padding(dlgPad),
                    verticalArrangement = Arrangement.spacedBy(
                        if (dlgFit.compactHomeChrome) 8.dp else 12.dp
                    )
                ) {
                    Text(
                        text = state.label.trim().ifBlank { "Facture" },
                        style = MaterialTheme.typography.titleLarge,
                        color = A11yTokens.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Image(
                        bitmap = pendingPreview!!.asImageBitmap(),
                        contentDescription = "Photo de la facture",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = if (dlgFit.compactHomeChrome) 220.dp else 280.dp,
                                max = if (dlgFit.compactHomeChrome) 420.dp else 520.dp
                            )
                            .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                    )
                    SecondaryA11yButton(
                        text = "Fermer",
                        onClick = { showReceiptDialog = false }
                    )
                }
            }
        }
    }

    EntryFormScaffold(
    identity = EntryFormIdentityUi(
        title = state.formTitle,
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = viewModel::setExpense
    ),
    recurrenceUi = EntryFormRecurrenceUi(
        recurring = state.recurrence != RecurrenceChoice.Once,
        showRecurring = true,
        recurrence = state.recurrence,
        onRecurrence = viewModel::setRecurrence,
        nextDueMonth = state.nextDueMonth,
        onNextDueMonth = viewModel::setNextDueMonth,
        showNextDueMonth = state.needsNextDueMonth,
        onRecurring = { on ->
            viewModel.setRecurrence(
                if (on) RecurrenceChoice.Monthly else RecurrenceChoice.Once
            )
        },
        showApplyFollowing = false,
        applyFollowing = false,
        hasTemplate = false,
        onApplyFollowing = {}
    ),
    payment = EntryFormPaymentUi(
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense,
        paymentNote = state.paymentNote,
        onPaymentNote = viewModel::setPaymentNote,
        showPaymentAssist = state.isExpense,
        hasPendingPhoto = state.hasPendingPhoto,
        hasPendingPdf = state.hasPendingPdf,
        receiptPreview = pendingPreview,
        ocrBusy = state.ocrBusy,
        onScanEpc = onScanEpc,
        onTakePhoto = if (state.isExpense) takePhoto else null,
        onPickPdf = if (state.isExpense) pickPdf else null,
        onClearPendingPdf = if (state.hasPendingPdf) viewModel::clearPendingPdf else null,
        onViewReceipt = if (pendingPreview != null) {
            { showReceiptDialog = true }
        } else {
            null
        },
        onCopyPaymentNote = viewModel::copyPaymentNote,
        onReadCommFromPhoto = if (state.isExpense && (state.hasPendingPhoto || pendingPreview != null)) {
            viewModel::readCommunicationFromPhoto
        } else {
            null
        }
    ),
    dossier = EntryFormDossierUi(
        showDossierSection = state.isExpense,
        dossierExpanded = state.dossierExpanded,
        onDossierExpanded = viewModel::setDossierExpanded,
        causeLabel = state.causeLabel,
        onCauseLabel = viewModel::setCauseLabel,
        openingRemainingText = state.openingRemainingText,
        onOpeningRemainingText = viewModel::setOpeningRemainingText,
        isAgreement = false,
        remainingCents = null,
        isSettled = false,
        iban = state.iban,
        onIban = viewModel::setIban,
        onCopyIban = viewModel::copyIban,
        creditorEmail = state.creditorEmail,
        onCreditorEmail = viewModel::setCreditorEmail,
        onSaveAsPendingLetter = viewModel::saveAsPendingLetter
    ),
    chrome = EntryFormChromeUi(
        infoMessage = state.copyMessage,
        error = state.error,
        primaryLabel = if (state.saving) "Enregistrement…" else "Enregistrer",
        primaryEnabled = !state.saving,
        deleteLabel = "Supprimer",
        onPrimary = viewModel::save,
        onCancel = { requestLeave() },
        onDelete = null
    )
)
}

@Composable
fun EditEntryScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    onScanEpc: () -> Unit = {},
    epcRawResult: String? = null,
    onEpcRawConsumed: () -> Unit = {},
    viewModel: EditEntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    var baseline by remember { mutableStateOf<EditEntryState?>(null) }
    LaunchedEffect(state.done) { if (state.done) onDone() }
    LaunchedEffect(state.loading) {
        if (!state.loading && baseline == null) baseline = state
    }
    LaunchedEffect(epcRawResult) {
        val raw = epcRawResult ?: return@LaunchedEffect
        viewModel.applyEpcRaw(raw)
        onEpcRawConsumed()
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.mailDraft) {
        val draft = state.mailDraft ?: return@LaunchedEffect
        val mailto = CreditorMailComposer.mailtoUri(draft)
        val title = be.monmoisclair.app.domain.SupportMailIntents.CREDITOR_CHOOSER_TITLE
        val canSendTo = be.monmoisclair.app.domain.SupportMailIntents.canResolveSendTo(
            context.packageManager,
            draft
        )
        val intent = if (canSendTo) {
            be.monmoisclair.app.domain.SupportMailIntents.sendToChooser(mailto, title)
        } else {
            be.monmoisclair.app.ui.clipboard.SensitiveClipboard.setPlainText(
                context,
                "E-mail",
                draft.to
            )
            be.monmoisclair.app.domain.SupportMailIntents.sendFallbackChooser(draft, title)
        }
        val launched = runCatching { context.startActivity(intent) }.isSuccess
        when {
            !launched -> viewModel.mailLaunchFailed()
            !canSendTo -> viewModel.mailLaunchUsedFallback()
            else -> viewModel.consumeMailDraft()
        }
    }
    val dirty = baseline?.let { b ->
        state.label != b.label ||
            state.amountText != b.amountText ||
            state.dayText != b.dayText ||
            state.paymentNote != b.paymentNote ||
            state.isAutoDebit != b.isAutoDebit ||
            state.recurrence != b.recurrence ||
            state.applyToFollowing != b.applyToFollowing ||
            // Collapse d’un accord existant ≠ modification (pas de wipe).
            (!b.isAgreement && state.dossierExpanded != b.dossierExpanded) ||
            state.causeLabel != b.causeLabel ||
            state.openingRemainingText != b.openingRemainingText ||
            state.iban != b.iban ||
            state.creditorEmail != b.creditorEmail ||
            state.feeAddText != b.feeAddText ||
            state.feeNoteText != b.feeNoteText ||
            state.hasPendingPhoto ||
            state.hasPendingPdf ||
            state.receiptCleared != b.receiptCleared ||
            state.receiptRelativePath != b.receiptRelativePath ||
            state.extraReceiptPaths != b.extraReceiptPaths
    } == true

    fun requestLeave() {
        if (dirty) confirmLeave = true else onCancel()
    }
    BackHandler { requestLeave() }
    if (confirmLeave) {
        DiscardChangesDialog(
            onStay = { confirmLeave = false },
            onLeave = {
                confirmLeave = false
                onCancel()
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
                        contentDescription = "Chargement de la fiche"
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
        val missFit = rememberDisplayFit()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .padding(A11yTokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(
                if (missFit.compactHomeChrome) 12.dp else 16.dp
            )
        ) {
            CarnetPageTitle("Cette ligne n’est plus là.", withTopSpacer = false)
            Text(
                text = "Elle a peut-être déjà été retirée. Retournez à l’accueil.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            SecondaryA11yButton(text = "Retour", onClick = onCancel)
        }
        return
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = A11yTokens.PaperCard,
            title = {
                val name = state.label.trim().ifBlank { "cette entrée" }
                Text(
                    text = if (state.isSavingsLine) "Retirer la ligne ?" else "Supprimer $name ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = when {
                        state.isSavingsLine ->
                            "La ligne ne reviendra plus les mois suivants. " +
                                "Les virements déjà marqués FAIT restent."
                        state.applyToFollowing && state.hasTemplate ->
                            "La ligne sera effacée, l’échéance ne reviendra plus, " +
                                "et les mois suivants encore « à payer » seront aussi retirés."
                        else ->
                            "Cette action retire la ligne du mois. Vous pourrez en recréer une ensuite."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = if (state.isSavingsLine) "Retirer" else "Supprimer",
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Annuler",
                    onClick = { confirmDelete = false },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }

    var pendingPreview by remember { mutableStateOf<Bitmap?>(null) }
    val takePhotoRaw = rememberFullPhotoCapture(
        onBitmap = { bmp ->
            pendingPreview = bmp
            viewModel.attachPreviewBitmap(bmp)
        },
        onDecodeFailed = { viewModel.setPhotoDecodeError() },
        onCaptureCancelled = {
            viewModel.setCameraCaptureError(EntryFormMessages.CAMERA_CAPTURE_CANCELLED)
        }
    )
    val takePhoto = be.monmoisclair.app.ui.camera.rememberCameraGatedLaunch(
        onGranted = takePhotoRaw,
        onMessage = viewModel::setCameraCaptureError,
        onOpenSettings = {
            if (!be.monmoisclair.app.ui.camera.openAppDetailsSettings(context)) {
                viewModel.setCameraCaptureError(
                    be.monmoisclair.app.ui.camera.CameraCaptureMessages.SETTINGS_OPEN_FAILED
                )
            }
        }
    )
    val pickPdf = rememberPdfPicker(
        onBytes = { bytes ->
            pendingPreview = null
            viewModel.attachPendingPdf(bytes)
        },
        onFailed = viewModel::setPdfReadError
    )
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    /** null = fermé ; "" = aperçu en attente / photo principale ; sinon chemin d’un courrier en plus. */
    var dialogPath by remember { mutableStateOf<String?>(null) }
    var dialogBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var dialogLoading by remember { mutableStateOf(false) }
    suspend fun openExternalReceipt(path: String) {
        val intent = viewModel.viewReceiptIntent(path)
        if (intent == null) {
            viewModel.setPdfReadError("Impossible d’ouvrir ce courrier.")
            return
        }
        val ok = runCatching {
            context.startActivity(Intent.createChooser(intent, "Voir le courrier"))
        }.isSuccess
        if (!ok) viewModel.setPdfReadError(be.monmoisclair.app.ui.ExternalIntentMessages.NO_PDF_VIEWER)
    }
    LaunchedEffect(state.receiptRelativePath, state.hasPendingPhoto, state.hasPendingPdf) {
        if (state.hasPendingPhoto || state.hasPendingPdf) return@LaunchedEffect
        if (state.receiptRelativePath.isBlank() || viewModel.isPdfPath(state.receiptRelativePath)) {
            receiptBitmap = null
            return@LaunchedEffect
        }
        receiptBitmap = viewModel.decodeReceiptBitmap()
    }
    LaunchedEffect(dialogPath, pendingPreview, receiptBitmap) {
        val path = dialogPath ?: run {
            dialogBitmap = null
            dialogLoading = false
            return@LaunchedEffect
        }
        val instant = when {
            path.isEmpty() -> pendingPreview ?: receiptBitmap
            path == state.receiptRelativePath -> pendingPreview ?: receiptBitmap
            else -> null
        }
        if (instant != null) {
            dialogBitmap = instant
            dialogLoading = false
            return@LaunchedEffect
        }
        dialogLoading = true
        dialogBitmap = null
        dialogBitmap = viewModel.decodeReceiptBitmap(path)
        dialogLoading = false
    }

    if (dialogPath != null) {
        val bmp = dialogBitmap
        Dialog(
            onDismissRequest = { dialogPath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = A11yTokens.PaperCream,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(A11yTokens.screenPadding)
            ) {
                val dlgFit = rememberDisplayFit()
                val dlgPad = if (dlgFit.compactHomeChrome) 12.dp else 16.dp
                Column(
                    modifier = Modifier.padding(dlgPad),
                    verticalArrangement = Arrangement.spacedBy(
                        if (dlgFit.compactHomeChrome) 8.dp else 12.dp
                    )
                ) {
                    Text(
                        text = state.label.trim().ifBlank { "Facture" },
                        style = MaterialTheme.typography.titleLarge,
                        color = A11yTokens.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    when {
                        dialogLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                                    .semantics {
                                        contentDescription = "Chargement de la photo"
                                        liveRegion = LiveRegionMode.Polite
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = A11yTokens.Link)
                            }
                        }
                        bmp != null -> {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Photo de la facture",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        min = if (dlgFit.compactHomeChrome) 220.dp else 280.dp,
                                        max = if (dlgFit.compactHomeChrome) 420.dp else 520.dp
                                    )
                                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                            )
                        }
                        else -> {
                            CarnetStateBanner(
                                text = "Impossible d’afficher cette photo ici. " +
                                    "Essayez « Ouvrir à part », ou reprenez-en une.",
                                kind = CarnetBannerKind.Error,
                                pad = 12.dp
                            )
                            val openPath = dialogPath?.takeIf { it.isNotBlank() }
                                ?: state.receiptRelativePath.takeIf { it.isNotBlank() }
                            if (openPath != null) {
                                SecondaryA11yButton(
                                    text = "Ouvrir à part",
                                    onClick = {
                                        scope.launch { openExternalReceipt(openPath) }
                                    }
                                )
                            }
                        }
                    }
                    SecondaryA11yButton(
                        text = "Fermer",
                        onClick = { dialogPath = null }
                    )
                }
            }
        }
    }

    EntryFormScaffold(
    identity = EntryFormIdentityUi(
        title = state.label.trim().ifBlank { "Modifier la facture" },
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = {}
    ),
    recurrenceUi = EntryFormRecurrenceUi(
        recurring = state.recurrence != RecurrenceChoice.Once,
        showRecurring = false,
        recurrence = state.recurrence,
        onRecurrence = viewModel::setRecurrence,
        showFrequencyEdit = (state.hasTemplate || state.isExpense) && !state.isSavingsLine,
        recurrenceInfo = state.recurrenceInfo,
        onRecurring = {},
        showApplyFollowing = state.hasTemplate && !state.isSavingsLine,
        applyFollowing = state.applyToFollowing,
        hasTemplate = state.hasTemplate,
        onApplyFollowing = viewModel::setApplyToFollowing
    ),
    payment = EntryFormPaymentUi(
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense && !state.isSavingsLine,
        paymentNote = state.paymentNote,
        onPaymentNote = viewModel::setPaymentNote,
        showPaymentAssist = state.isExpense,
        hasPendingPhoto = state.hasPendingPhoto,
        hasPendingPdf = state.hasPendingPdf,
        hasReceiptPhoto = state.receiptRelativePath.isNotBlank(),
        receiptIsPdf = state.receiptRelativePath.isNotBlank() &&
            viewModel.isPdfPath(state.receiptRelativePath),
        receiptPreview = pendingPreview ?: receiptBitmap,
        extraReceiptPaths = ReceiptPathList.parse(state.extraReceiptPaths),
        isPdfPath = viewModel::isPdfPath,
        ocrBusy = state.ocrBusy,
        onScanEpc = onScanEpc,
        onTakePhoto = if (
            !state.futurePreviewReadOnly &&
            state.isExpense &&
            !state.isSavingsLine
        ) {
            takePhoto
        } else {
            null
        },
        onPickPdf = if (
            !state.futurePreviewReadOnly &&
            state.isExpense &&
            !state.isSavingsLine
        ) {
            pickPdf
        } else {
            null
        },
        onClearPendingPdf = if (!state.futurePreviewReadOnly) viewModel::clearPendingPdf else null,
        onClearReceipt = if (
            !state.futurePreviewReadOnly &&
            (state.receiptRelativePath.isNotBlank() || state.hasPendingPhoto)
        ) {
            {
                pendingPreview = null
                viewModel.clearReceiptPhoto()
            }
        } else {
            null
        },
        onViewReceipt = if (
            state.receiptRelativePath.isNotBlank() || pendingPreview != null
        ) {
            {
                when {
                    pendingPreview != null -> dialogPath = ""
                    viewModel.isPdfPath(state.receiptRelativePath) ->
                        scope.launch { openExternalReceipt(state.receiptRelativePath) }
                    else -> dialogPath = state.receiptRelativePath
                }
            }
        } else {
            null
        },
        onViewExtraReceipt = { path ->
            if (viewModel.isPdfPath(path)) {
                scope.launch { openExternalReceipt(path) }
            } else {
                dialogPath = path
            }
        },
        onRemoveExtraReceipt = if (!state.futurePreviewReadOnly) {
            viewModel::removeExtraReceipt
        } else {
            null
        },
        onCopyPaymentNote = viewModel::copyPaymentNote,
        onReadCommFromPhoto = if (
            !state.futurePreviewReadOnly &&
            state.isExpense &&
            !state.isSavingsLine &&
            (
                state.hasPendingPhoto ||
                    (state.receiptRelativePath.isNotBlank() && !viewModel.isPdfPath(state.receiptRelativePath))
                )
        ) {
            viewModel::readCommunicationFromPhoto
        } else {
            null
        }
    ),
    dossier = EntryFormDossierUi(
        showDossierSection = state.isExpense && !state.isSavingsLine,
        dossierExpanded = state.dossierExpanded,
        onDossierExpanded = viewModel::setDossierExpanded,
        causeLabel = state.causeLabel,
        onCauseLabel = viewModel::setCauseLabel,
        openingRemainingText = state.openingRemainingText,
        onOpeningRemainingText = viewModel::setOpeningRemainingText,
        isAgreement = state.isAgreement,
        remainingCents = state.remainingCents,
        isSettled = state.isSettled,
        pacePhrase = state.pacePhrase,
        iban = state.iban,
        onIban = viewModel::setIban,
        onCopyIban = viewModel::copyIban,
        creditorEmail = state.creditorEmail,
        onCreditorEmail = viewModel::setCreditorEmail,
        onWriteMail = viewModel::prepareMail,
        feeAddText = state.feeAddText,
        onFeeAddText = viewModel::setFeeAddText,
        feeNoteText = state.feeNoteText,
        onFeeNoteText = viewModel::setFeeNoteText,
        addedFeesCents = state.addedFeesCents,
        feeEvents = state.feeEvents
    ),
    savings = EntryFormSavingsUi(
        isSavingsLine = state.isSavingsLine,
        savingsTransferredCents = state.savingsTransferredCents,
        lockAmountAndDay = state.isSavingsLine && state.savingsMonthDone
    ),
    chrome = EntryFormChromeUi(
        infoMessage = state.copyMessage,
        error = state.error,
        primaryLabel = if (state.saving) "Enregistrement…" else "Enregistrer",
        primaryEnabled = !state.saving && !state.futurePreviewReadOnly,
        deleteLabel = if (state.isSavingsLine) "Retirer la ligne" else "Supprimer",
        onPrimary = viewModel::save,
        onCancel = { requestLeave() },
        onDelete = {
            if (!state.saving && !state.futurePreviewReadOnly) confirmDelete = true
        }
    )
)
}

