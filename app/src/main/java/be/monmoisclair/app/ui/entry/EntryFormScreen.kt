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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.CreditorMailComposer
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import java.io.File

/** Capture plein cadre (pas la miniature TakePicturePreview). */
@Composable
private fun rememberFullPhotoCapture(
    onBitmap: (Bitmap) -> Unit,
    onDecodeFailed: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val file = pendingFile
        pendingFile = null
        if (!ok) {
            // E08 : annulation → effacer le jpg temporaire.
            file?.delete()
            return@rememberLauncherForActivityResult
        }
        if (file == null || !file.exists()) return@rememberLauncherForActivityResult
        val decoded = runCatching { decodeCameraJpegBounded(file) }.getOrNull()
        file.delete()
        if (decoded != null) onBitmap(decoded) else onDecodeFailed()
    }
    return {
        val cameraDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        val file = File(cameraDir, "receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        pendingFile = file
        launcher.launch(uri)
    }
}

/** Évite OOM sur un JPEG caméra très lourd (borne ~2048 px). */
private fun decodeCameraJpegBounded(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (longest / sample > 2048) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.absolutePath, opts)
}

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
    var pendingPreview by remember { mutableStateOf<Bitmap?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }
    val takePhoto = rememberFullPhotoCapture(
        onBitmap = { bmp ->
            pendingPreview = bmp
            viewModel.attachPreviewBitmap(bmp)
        },
        onDecodeFailed = {
            viewModel.setPhotoDecodeError()
        }
    )
    var showReceiptDialog by remember { mutableStateOf(false) }
    // Déplier « retard ou accord » sans rien saisir ≠ sale (C6 / Senior :
    // curiosité puis Annuler ne doit pas ouvrir « quitter sans sauver »).
    val dirty = state.label.isNotBlank() ||
        state.amountText.isNotBlank() ||
        state.paymentNote.isNotBlank() ||
        state.hasPendingPhoto ||
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

    LaunchedEffect(state.saved) { if (state.saved) onDone() }
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
                    PrimaryA11yButton(
                        text = "Fermer",
                        onClick = { showReceiptDialog = false }
                    )
                }
            }
        }
    }

    EntryFormScaffold(
        title = state.formTitle,
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        recurring = state.recurrence != RecurrenceChoice.Once,
        showRecurring = true,
        recurrence = state.recurrence,
        onRecurrence = viewModel::setRecurrence,
        nextDueMonth = state.nextDueMonth,
        onNextDueMonth = viewModel::setNextDueMonth,
        showNextDueMonth = state.needsNextDueMonth,
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense,
        paymentNote = state.paymentNote,
        onPaymentNote = viewModel::setPaymentNote,
        showPaymentAssist = state.isExpense,
        hasPendingPhoto = state.hasPendingPhoto,
        receiptPreview = pendingPreview,
        onScanEpc = onScanEpc,
        onTakePhoto = takePhoto,
        onViewReceipt = if (pendingPreview != null) {
            { showReceiptDialog = true }
        } else {
            null
        },
        onCopyPaymentNote = viewModel::copyPaymentNote,
        showApplyFollowing = false,
        applyFollowing = false,
        hasTemplate = false,
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
        creditorEmail = state.creditorEmail,
        onCreditorEmail = viewModel::setCreditorEmail,
        onSaveAsPendingLetter = viewModel::saveAsPendingLetter,
        infoMessage = state.copyMessage,
        error = state.error,
        primaryLabel = if (state.saving) "Enregistrement…" else "Enregistrer",
        primaryEnabled = !state.saving,
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = viewModel::setExpense,
        onRecurring = { on ->
            viewModel.setRecurrence(
                if (on) RecurrenceChoice.Monthly else RecurrenceChoice.Once
            )
        },
        onApplyFollowing = {},
        onPrimary = viewModel::save,
        onCancel = { requestLeave() },
        onDelete = null
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
    LaunchedEffect(state.mailDraft) {
        val draft = state.mailDraft ?: return@LaunchedEffect
        // Gmail ignore souvent EXTRA_* sur SENDTO : tout va dans l’URI mailto.
        val intent = Intent(
            Intent.ACTION_SENDTO,
            Uri.parse(CreditorMailComposer.mailtoUri(draft))
        )
        val launched = runCatching {
            context.startActivity(Intent.createChooser(intent, "Écrire"))
        }.isSuccess
        if (launched) viewModel.consumeMailDraft() else viewModel.mailLaunchFailed()
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
            val busyShape = MaterialTheme.shapes.medium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperCard, busyShape)
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Chargement de la fiche"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.ButtonFill,
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
            Text(
                text = "Cette ligne n’est plus là.",
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink
            )
            val missShape = MaterialTheme.shapes.medium
            Text(
                text = "Elle a peut-être déjà été retirée. Retournez à l’accueil.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, missShape)
                    .background(A11yTokens.PaperCard, missShape)
                    .padding(if (missFit.compactHomeChrome) 12.dp else 14.dp)
            )
            SecondaryA11yButton(text = "Retour", onClick = onCancel)
        }
        return
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
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
    val takePhoto = rememberFullPhotoCapture(
        onBitmap = { bmp ->
            pendingPreview = bmp
            viewModel.attachPreviewBitmap(bmp)
        },
        onDecodeFailed = { viewModel.setPhotoDecodeError() }
    )
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    /** null = fermé ; "" = aperçu en attente / photo principale ; sinon chemin d’un courrier en plus. */
    var dialogPath by remember { mutableStateOf<String?>(null) }
    var dialogBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var dialogLoading by remember { mutableStateOf(false) }
    LaunchedEffect(state.receiptRelativePath, state.hasPendingPhoto) {
        if (state.hasPendingPhoto) return@LaunchedEffect
        if (state.receiptRelativePath.isBlank()) {
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
                                CircularProgressIndicator(color = A11yTokens.ButtonFill)
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
                            val photoErrShape = MaterialTheme.shapes.medium
                            val photoErr =
                                "Impossible d’ouvrir cette photo. " +
                                    "Reprenez-en une avec le bouton ci-dessous."
                            Text(
                                text = photoErr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.AccentDanger,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        A11yTokens.AccentDanger.copy(alpha = 0.4f),
                                        photoErrShape
                                    )
                                    .background(A11yTokens.PaperWarm, photoErrShape)
                                    .padding(12.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                        contentDescription = photoErr
                                    }
                            )
                        }
                    }
                    PrimaryA11yButton(
                        text = "Fermer",
                        onClick = { dialogPath = null }
                    )
                }
            }
        }
    }

    EntryFormScaffold(
        title = state.label.trim().ifBlank { "Modifier la facture" },
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        recurring = state.recurrence != RecurrenceChoice.Once,
        showRecurring = false,
        showFrequencyEdit = (state.hasTemplate || state.isExpense) && !state.isSavingsLine,
        recurrence = state.recurrence,
        onRecurrence = viewModel::setRecurrence,
        recurrenceInfo = state.recurrenceInfo,
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense && !state.isSavingsLine,
        paymentNote = state.paymentNote,
        onPaymentNote = viewModel::setPaymentNote,
        showPaymentAssist = state.isExpense,
        hasPendingPhoto = state.hasPendingPhoto,
        hasReceiptPhoto = state.receiptRelativePath.isNotBlank(),
        receiptPreview = pendingPreview ?: receiptBitmap,
        onScanEpc = onScanEpc,
        onTakePhoto = takePhoto,
        onClearReceipt = if (
            state.receiptRelativePath.isNotBlank() || state.hasPendingPhoto
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
                dialogPath = if (pendingPreview != null) "" else state.receiptRelativePath
            }
        } else {
            null
        },
        extraReceiptPaths = ReceiptPathList.parse(state.extraReceiptPaths),
        onViewExtraReceipt = { path -> dialogPath = path },
        onRemoveExtraReceipt = viewModel::removeExtraReceipt,
        onCopyPaymentNote = viewModel::copyPaymentNote,
        showApplyFollowing = state.hasTemplate && !state.isSavingsLine,
        applyFollowing = state.applyToFollowing,
        hasTemplate = state.hasTemplate,
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
        isSavingsLine = state.isSavingsLine,
        savingsTransferredCents = state.savingsTransferredCents,
        lockAmountAndDay = state.isSavingsLine && state.savingsMonthDone,
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
        feeEvents = state.feeEvents,
        infoMessage = state.copyMessage,
        error = state.error,
        primaryLabel = if (state.saving) "Enregistrement…" else "Enregistrer",
        primaryEnabled = !state.saving,
        deleteLabel = if (state.isSavingsLine) "Retirer la ligne" else "Supprimer",
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = {},
        onRecurring = {},
        onApplyFollowing = viewModel::setApplyToFollowing,
        onPrimary = viewModel::save,
        onCancel = { requestLeave() },
        onDelete = { if (!state.saving) confirmDelete = true }
    )
}

@Composable
private fun DiscardChangesDialog(
    onStay: () -> Unit,
    onLeave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStay,
        shape = MaterialTheme.shapes.medium,
        containerColor = A11yTokens.PaperWarm,
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
private fun EntryFormScaffold(
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
    hasReceiptPhoto: Boolean = false,
    onScanEpc: (() -> Unit)? = null,
    onTakePhoto: (() -> Unit)? = null,
    onClearReceipt: (() -> Unit)? = null,
    onViewReceipt: (() -> Unit)? = null,
    /** Courriers en plus (dossier) — chemins relatifs déjà parsés. */
    extraReceiptPaths: List<String> = emptyList(),
    onViewExtraReceipt: ((String) -> Unit)? = null,
    onRemoveExtraReceipt: ((String) -> Unit)? = null,
    onCopyPaymentNote: (() -> Unit)? = null,
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
    val formGap = if (fit.compactHomeChrome) 8.dp else 12.dp
    val tipPad = if (fit.compactHomeChrome) 10.dp else 12.dp
    val tipPadLg = if (fit.compactHomeChrome) 12.dp else 14.dp
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
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink
            )

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
                val savingsShape = MaterialTheme.shapes.medium
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, savingsShape)
                        .background(A11yTokens.PaperWarm, savingsShape)
                        .padding(tipPad),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ce n’est pas de l’argent gardé dans l’application.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                    if (savingsTransferredCents > 0L) {
                        Text(
                            text = "Déjà viré : ${MoneyFormat.formatCents(savingsTransferredCents)} " +
                                "— pas disponible ici.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                    }
                    if (lockAmountAndDay) {
                        Text(
                            text = "Ce mois est déjà FAIT. Le montant affiché est celui du virement. " +
                                "Pour les mois suivants, changez-le dans Réglages.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                    }
                }
            }
            // Accords : reste + rythme au-dessus de la grille (campagne D2 / D11 —
            // ne plus exiger de scroller sous les 31 jours).
            if (isAgreement && !isSavingsLine) {
                val remShape = MaterialTheme.shapes.medium
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, remShape)
                        .background(A11yTokens.PaperWarm, remShape)
                        .padding(tipPad),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSettled) {
                        Text(
                            text = "Soldé",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.AccentOk
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

            // Édition ponctuelle (sans modèle) : info seule.
            if (!showRecurring && !showFrequencyEdit) {
                recurrenceInfo?.let { info ->
                    Text(text = "Fréquence", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
                    Text(text = info, style = MaterialTheme.typography.bodyLarge)
                }
            }

            if ((showRecurring || showFrequencyEdit) && onRecurrence != null) {
                Text("Fréquence", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
                if (showFrequencyEdit) {
                    val freqTipShape = MaterialTheme.shapes.medium
                    Text(
                        text = "Vous pouvez corriger une erreur (mensuel, 3 mois, annuel, une fois) " +
                            "sans supprimer la ligne. Pour les mois déjà créés : cochez " +
                            "« Aussi les mois suivants ».",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, A11yTokens.PaperBorder, freqTipShape)
                            .background(A11yTokens.PaperWarm, freqTipShape)
                            .padding(tipPad)
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
                    Text("Prochaine échéance", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
                    val nextDueTipShape = MaterialTheme.shapes.medium
                    Text(
                        text = "Choisissez le mois où elle tombe (ex. taxe en mars).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, A11yTokens.PaperBorder, nextDueTipShape)
                            .background(A11yTokens.PaperWarm, nextDueTipShape)
                            .padding(10.dp)
                    )
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
            if (showAutoDebit) {
                CheckRow(
                    checked = isAutoDebit,
                    label = "Prélèvement automatique",
                    onChecked = onAutoDebit
                )
                val autoTipShape = MaterialTheme.shapes.medium
                Text(
                    text = "Les rappels diront de vérifier le prélèvement (la ligne reste à cocher FAIT).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, autoTipShape)
                        .background(A11yTokens.PaperCard, autoTipShape)
                        .padding(tipPad)
                )
            }
            if (showDossierSection) {
                SecondaryA11yButton(
                    text = if (dossierExpanded) {
                        "C’est un retard ou un accord ? ▴"
                    } else {
                        "C’est un retard ou un accord ? ▾"
                    },
                    onClick = { onDossierExpanded(!dossierExpanded) }
                )
                if (dossierExpanded) {
                    val dossierShape = MaterialTheme.shapes.medium
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, A11yTokens.PaperBorder, dossierShape)
                            .background(A11yTokens.PaperCard, dossierShape)
                            .padding(tipPad),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    if (isSettled) {
                        val settledShape = MaterialTheme.shapes.medium
                        Text(
                            text = "Soldé",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.AccentOk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.AccentOk.copy(alpha = 0.35f), settledShape)
                                .background(A11yTokens.PaperWarm, settledShape)
                                .padding(tipPad)
                        )
                    }
                    remainingCents?.let { rem ->
                        if (!isSettled) {
                            val remShape = MaterialTheme.shapes.medium
                            Text(
                                text = "Encore ${MoneyFormat.formatCents(rem)} à rembourser.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, A11yTokens.PaperBorder, remShape)
                                    .background(A11yTokens.PaperWarm, remShape)
                                    .padding(10.dp)
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
                        shape = MaterialTheme.shapes.medium
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        val openTipShape = MaterialTheme.shapes.medium
                        Text(
                            text = "Le montant plus haut est le versement du mois. " +
                                "Laissez vide si ce n’est pas un accord.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.PaperBorder, openTipShape)
                                .background(A11yTokens.PaperCard, openTipShape)
                                .padding(10.dp)
                        )
                        if (onSaveAsPendingLetter != null &&
                            openingRemainingText.isBlank()
                        ) {
                            SecondaryA11yButton(
                                text = "Ranger sans plan",
                                onClick = onSaveAsPendingLetter,
                                enabled = primaryEnabled
                            )
                            val pendingTipShape = MaterialTheme.shapes.medium
                            Text(
                                text = "Photo et nom seulement — rien dans le Reste " +
                                    "tant que le versement n’est pas fixé.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, A11yTokens.PaperBorder, pendingTipShape)
                                    .background(A11yTokens.PaperWarm, pendingTipShape)
                                    .padding(10.dp)
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                    if (onCopyIban != null && iban.isNotBlank()) {
                        SecondaryA11yButton(text = "Copier l’IBAN", onClick = onCopyIban)
                        val ibanTipShape = MaterialTheme.shapes.medium
                        Text(
                            text = "Pour coller dans l’app bancaire.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.PaperBorder, ibanTipShape)
                                .background(A11yTokens.PaperWarm, ibanTipShape)
                                .padding(10.dp)
                        )
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                    if (onWriteMail != null && creditorEmail.isNotBlank()) {
                        SecondaryA11yButton(text = "Écrire", onClick = onWriteMail)
                        val writeTipShape = MaterialTheme.shapes.medium
                        Text(
                            text = "Ouvre l’app Mail avec un brouillon. Rien n’est stocké ici.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.PaperBorder, writeTipShape)
                                .background(A11yTokens.PaperWarm, writeTipShape)
                                .padding(10.dp)
                        )
                    }
                    if (isAgreement) {
                        if (feeEvents.isNotEmpty() || addedFeesCents > 0L) {
                            val feesShape = MaterialTheme.shapes.medium
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, A11yTokens.PaperBorder, feesShape)
                                    .background(A11yTokens.PaperWarm, feesShape)
                                    .padding(10.dp),
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
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                    } // fin carte dossier
                }
            }
            if (showPaymentAssist) {
                val paperShape = MaterialTheme.shapes.medium
                Text("Facture papier", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, paperShape)
                        .background(A11yTokens.PaperCard, paperShape)
                        .padding(tipPad),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                if (onScanEpc != null) {
                    SecondaryA11yButton(text = "Scanner une facture (QR)", onClick = onScanEpc)
                    val scanTipShape = MaterialTheme.shapes.medium
                    Text(
                        text = "Visez le QR de la facture belge.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, A11yTokens.PaperBorder, scanTipShape)
                            .background(A11yTokens.PaperWarm, scanTipShape)
                            .padding(10.dp)
                    )
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
                    shape = MaterialTheme.shapes.medium
                )
                if (onCopyPaymentNote != null && paymentNote.isNotBlank()) {
                    SecondaryA11yButton(text = "Copier la communication", onClick = onCopyPaymentNote)
                }
                if (onTakePhoto != null) {
                    val appendOnAgreement = isAgreement && hasReceiptPhoto && !hasPendingPhoto
                    SecondaryA11yButton(
                        text = when {
                            hasPendingPhoto -> "Reprendre la photo"
                            appendOnAgreement -> "Ajouter un courrier (photo)"
                            hasReceiptPhoto -> "Reprendre la photo de la facture"
                            else -> "Photo de la facture papier"
                        },
                        onClick = onTakePhoto
                    )
                    if (appendOnAgreement) {
                        val appendTipShape = MaterialTheme.shapes.medium
                        Text(
                            text = "La nouvelle photo s’ajoute aux courriers. " +
                                "La première reste.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.PaperBorder, appendTipShape)
                                .background(A11yTokens.PaperWarm, appendTipShape)
                                .padding(10.dp)
                        )
                    }
                    if (onClearReceipt != null && (hasPendingPhoto || hasReceiptPhoto)) {
                        SecondaryA11yButton(
                            text = if (
                                !hasPendingPhoto &&
                                hasReceiptPhoto &&
                                !applyFollowing &&
                                !isSavingsLine &&
                                (isAgreement || hasTemplate)
                            ) {
                                "Enlever la photo de ce mois"
                            } else {
                                "Enlever la photo"
                            },
                            onClick = onClearReceipt
                        )
                    }
                    if (hasPendingPhoto) {
                        val photoOkShape = MaterialTheme.shapes.medium
                        val photoOkText = if (isAgreement && hasReceiptPhoto) {
                            "Photo prête. Elle s’ajoutera aux courriers quand vous enregistrerez."
                        } else {
                            "Photo prête. Touchez Enregistrer pour la garder."
                        }
                        Text(
                            text = photoOkText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.AccentOk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    A11yTokens.AccentOk.copy(alpha = 0.35f),
                                    photoOkShape
                                )
                                .background(A11yTokens.AccentOkSoft, photoOkShape)
                                .padding(10.dp)
                                .semantics {
                                    liveRegion = LiveRegionMode.Polite
                                    contentDescription = photoOkText
                                }
                        )
                    }
                    if (hasReceiptPhoto && receiptPreview == null && onViewReceipt != null) {
                        SecondaryA11yButton(
                            text = "Voir la photo de la facture",
                            onClick = onViewReceipt
                        )
                    }
                    receiptPreview?.let { bmp ->
                        val zoomTipShape = MaterialTheme.shapes.medium
                        Text(
                            text = "Touchez pour agrandir.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.PaperBorder, zoomTipShape)
                                .background(A11yTokens.PaperWarm, zoomTipShape)
                                .padding(10.dp)
                        )
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Photo de la facture",
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
                                            onClickLabel = "Voir la facture",
                                            onClick = onViewReceipt
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                    if (isAgreement && extraReceiptPaths.isNotEmpty()) {
                        Text(
                            text = if (hasReceiptPhoto) "Autres courriers" else "Courriers",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.InkMuted
                        )
                        extraReceiptPaths.forEachIndexed { index, path ->
                            val n = if (hasReceiptPhoto) index + 2 else index + 1
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (onViewExtraReceipt != null) {
                                    SecondaryA11yButton(
                                        text = "Voir le courrier $n",
                                        onClick = { onViewExtraReceipt(path) },
                                        fillMaxWidth = false
                                    )
                                }
                                if (onRemoveExtraReceipt != null) {
                                    SecondaryA11yButton(
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
            }
            if (showApplyFollowing && hasTemplate) {
                CheckRow(
                    checked = applyFollowing,
                    label = "Aussi les mois suivants",
                    onChecked = onApplyFollowing
                )
                val followShape = MaterialTheme.shapes.medium
                Text(
                    text = "Laissé décoché : montant et photo de ce mois seulement. " +
                        "La communication suit déjà les mois suivants.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, followShape)
                        .background(A11yTokens.PaperWarm, followShape)
                        .padding(tipPad)
                )
            }

            infoMessage?.let {
                val infoShape = MaterialTheme.shapes.medium
                val warn = it == BelgianStructuredCommunication.VERIFY_ON_MAIL
                Text(
                    text = it,
                    color = if (warn) A11yTokens.AccentWarn else A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (warn) A11yTokens.AccentWarn.copy(alpha = 0.4f)
                            else A11yTokens.AccentOk.copy(alpha = 0.35f),
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
            // Marge basse dans le scroll : le dernier rang de jours / champs
            // ne reste pas à moitié coupé juste au-dessus d’Enregistrer.
            Spacer(modifier = Modifier.height(A11yTokens.minTouchHeight))
        }

        error?.let {
            val errShape = MaterialTheme.shapes.medium
            Text(
                text = it,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentDanger.copy(alpha = 0.4f), errShape)
                    .background(A11yTokens.PaperWarm, errShape)
                    .padding(tipPad)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = it
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        PrimaryA11yButton(text = primaryLabel, onClick = onPrimary, enabled = primaryEnabled)
        if (!keyboardOpen) {
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryA11yButton(
                text = "Annuler",
                onClick = onCancel,
                enabled = primaryEnabled
            )
            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                DangerA11yButton(
                    text = deleteLabel,
                    onClick = onDelete,
                    enabled = primaryEnabled
                )
            }
        }
    }
}

@Composable
private fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val fit = rememberDisplayFit()
    val hPad = if (fit.compactHomeChrome) 12.dp else 16.dp
    val vPad = if (fit.compactHomeChrome) 10.dp else 12.dp
    val shape = MaterialTheme.shapes.medium
    val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
    val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(shape)
            .background(bg, shape)
            .border(
                1.dp,
                if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                shape
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = A11yTokens.minTouchHeight)
            .padding(horizontal = hPad, vertical = vPad)
            .semantics {
                contentDescription = if (selected) "$text, sélectionné" else text
            }
    )
}

@Composable
private fun CheckRow(checked: Boolean, label: String, onChecked: (Boolean) -> Unit) {
    // Checkbox avec onCheckedChange réel : le pattern toggleable+null
    // n’enregistre pas les taps sur One UI / uiautomator (case « Revient… » morte).
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .border(1.dp, A11yTokens.PaperBorder, shape)
            .background(A11yTokens.PaperCard, shape)
            .clickable(role = Role.Checkbox) { onChecked(!checked) }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .semantics {
                contentDescription = if (checked) "$label, coché" else "$label, non coché"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.Ink
        )
    }
}
