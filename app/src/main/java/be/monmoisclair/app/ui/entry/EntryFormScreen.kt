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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
    val dirty = state.label.isNotBlank() ||
        state.amountText.isNotBlank() ||
        state.paymentNote.isNotBlank() ||
        state.hasPendingPhoto ||
        pendingPreview != null ||
        state.recurrence != RecurrenceChoice.Once ||
        state.isAutoDebit ||
        state.dossierExpanded ||
        state.causeLabel.isNotBlank() ||
        state.openingRemainingText.isNotBlank() ||
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.label.trim().ifBlank { "Facture" },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Image(
                        bitmap = pendingPreview!!.asImageBitmap(),
                        contentDescription = "Photo de la facture",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 280.dp, max = 520.dp)
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
                .padding(A11yTokens.screenPadding)
                .semantics {
                    contentDescription = "Chargement de la fiche"
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            Text("Chargement…", style = MaterialTheme.typography.bodyLarge)
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
                Text("Supprimer $name ?")
            },
            text = {
                Text(
                    if (state.applyToFollowing && state.hasTemplate) {
                        "La ligne sera effacée, l’échéance ne reviendra plus, et les mois suivants encore « à payer » seront aussi retirés."
                    } else {
                        "Cette action retire la ligne du mois. Vous pourrez en recréer une ensuite."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Supprimer",
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
            return@LaunchedEffect
        }
        dialogBitmap = when {
            path.isEmpty() -> pendingPreview ?: receiptBitmap
            path == state.receiptRelativePath -> pendingPreview ?: receiptBitmap
            else -> viewModel.decodeReceiptBitmap(path)
        }
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.label.trim().ifBlank { "Facture" },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Photo de la facture",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 520.dp)
                                .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                        )
                    } else {
                        Text(
                            text = "Photo introuvable dans Documents. " +
                                "Reprenez-en une avec le bouton ci-dessous.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.AccentDanger
                        )
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
        title = "Modifier",
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        recurring = state.recurrence != RecurrenceChoice.Once,
        showRecurring = false,
        showFrequencyEdit = state.hasTemplate || state.isExpense,
        recurrence = state.recurrence,
        onRecurrence = viewModel::setRecurrence,
        recurrenceInfo = state.recurrenceInfo,
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense,
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
        showApplyFollowing = state.hasTemplate,
        applyFollowing = state.applyToFollowing,
        hasTemplate = state.hasTemplate,
        showDossierSection = state.isExpense,
        dossierExpanded = state.dossierExpanded,
        onDossierExpanded = viewModel::setDossierExpanded,
        causeLabel = state.causeLabel,
        onCauseLabel = viewModel::setCauseLabel,
        openingRemainingText = state.openingRemainingText,
        onOpeningRemainingText = viewModel::setOpeningRemainingText,
        isAgreement = state.isAgreement,
        remainingCents = state.remainingCents,
        isSettled = state.isSettled,
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
        addedFeesNote = state.addedFeesNote,
        infoMessage = state.copyMessage,
        error = state.error,
        primaryLabel = "Enregistrer",
        primaryEnabled = true,
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = {},
        onRecurring = {},
        onApplyFollowing = viewModel::setApplyToFollowing,
        onPrimary = viewModel::save,
        onCancel = { requestLeave() },
        onDelete = { confirmDelete = true }
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
        title = { Text("Quitter sans enregistrer ?") },
        text = {
            Text(
                "Les changements ne seront pas gardés.",
                style = MaterialTheme.typography.bodyLarge
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
    iban: String = "",
    onIban: (String) -> Unit = {},
    onCopyIban: (() -> Unit)? = null,
    creditorEmail: String = "",
    onCreditorEmail: (String) -> Unit = {},
    onWriteMail: (() -> Unit)? = null,
    feeAddText: String = "",
    onFeeAddText: (String) -> Unit = {},
    feeNoteText: String = "",
    onFeeNoteText: (String) -> Unit = {},
    addedFeesCents: Long = 0L,
    addedFeesNote: String = "",
    infoMessage: String? = null,
    error: String?,
    primaryLabel: String,
    primaryEnabled: Boolean,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)

            if (showRecurring) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                singleLine = true
            )
            DayOfMonthGrid(
                selectedDay = dayText.trim().toIntOrNull()?.takeIf { it in 1..31 },
                onSelect = { onDay(it.toString()) }
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
                    Text(
                        text = "Vous pouvez corriger une erreur (mensuel, 3 mois, annuel, une fois) " +
                            "sans supprimer la ligne. Pour les mois déjà créés : cochez " +
                            "« Aussi les mois suivants ».",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
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
                    Text(
                        text = "Choisissez le mois où elle tombe (ex. taxe en mars).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
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
                Text(
                    text = "Les rappels diront de vérifier le prélèvement (la ligne reste à cocher FAIT).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
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
                    if (isSettled) {
                        Text(
                            text = "Soldé",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.AccentOk
                        )
                    }
                    remainingCents?.let { rem ->
                        if (!isSettled) {
                            Text(
                                text = "Encore ${MoneyFormat.formatCents(rem)} à rembourser.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted
                            )
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
                        Text(
                            text = "Le montant plus haut est le versement du mois. " +
                                "Laissez vide si ce n’est pas un accord.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
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
                        shape = MaterialTheme.shapes.medium
                    )
                    if (onCopyIban != null && iban.isNotBlank()) {
                        SecondaryA11yButton(text = "Copier l’IBAN", onClick = onCopyIban)
                    }
                    OutlinedTextField(
                        value = creditorEmail,
                        onValueChange = onCreditorEmail,
                        label = { Text("E-mail du créancier") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = A11yTokens.minTouchHeight),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    if (onWriteMail != null) {
                        SecondaryA11yButton(text = "Écrire", onClick = onWriteMail)
                    }
                    if (isAgreement) {
                        if (addedFeesCents > 0L) {
                            Text(
                                text = "Frais déjà notés : ${MoneyFormat.formatCents(addedFeesCents)}" +
                                    if (addedFeesNote.isNotBlank()) " ($addedFeesNote)" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted
                            )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
            if (showPaymentAssist) {
                Text("Facture papier", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
                if (onScanEpc != null) {
                    SecondaryA11yButton(text = "Scanner une facture (QR)", onClick = onScanEpc)
                    Text(
                        text = "Visez le QR de la facture belge.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                }
                OutlinedTextField(
                    value = paymentNote,
                    onValueChange = onPaymentNote,
                    label = { Text("Communication / note") },
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
                        Text(
                            text = "La nouvelle photo s’ajoute aux courriers. " +
                                "La première reste.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                    }
                    if (onClearReceipt != null && (hasPendingPhoto || hasReceiptPhoto)) {
                        SecondaryA11yButton(
                            text = if (isAgreement && hasReceiptPhoto && !hasPendingPhoto) {
                                "Enlever la photo de ce mois"
                            } else {
                                "Enlever la photo"
                            },
                            onClick = onClearReceipt
                        )
                    }
                    if (hasPendingPhoto) {
                        Text(
                            text = if (isAgreement && hasReceiptPhoto) {
                                "Photo prête — s’ajoute aux courriers (la première reste)."
                            } else {
                                "Photo prête — enregistrée avec la facture."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.AccentOk
                        )
                    }
                    if (hasReceiptPhoto && receiptPreview == null && onViewReceipt != null) {
                        SecondaryA11yButton(
                            text = "Voir la photo de la facture",
                            onClick = onViewReceipt
                        )
                    }
                    receiptPreview?.let { bmp ->
                        Text(
                            text = "Touchez pour agrandir.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
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
                            text = "Autres courriers",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.InkMuted
                        )
                        extraReceiptPaths.forEachIndexed { index, path ->
                            val n = index + 2
                            if (onViewExtraReceipt != null) {
                                SecondaryA11yButton(
                                    text = "Voir le courrier $n",
                                    onClick = { onViewExtraReceipt(path) }
                                )
                            }
                            if (onRemoveExtraReceipt != null) {
                                SecondaryA11yButton(
                                    text = "Enlever le courrier $n",
                                    onClick = { onRemoveExtraReceipt(path) }
                                )
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
                Text(
                    text = "Laissé décoché : montant et photo de ce mois seulement. " +
                        "La communication suit déjà les mois suivants.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
            }

            infoMessage?.let {
                Text(
                    text = it,
                    color = if (it == BelgianStructuredCommunication.VERIFY_ON_MAIL) {
                        A11yTokens.AccentWarn
                    } else {
                        A11yTokens.AccentOk
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = it
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        error?.let {
            Text(
                text = it,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = it
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        PrimaryA11yButton(text = primaryLabel, onClick = onPrimary, enabled = primaryEnabled)
        if (!keyboardOpen) {
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryA11yButton(text = "Annuler", onClick = onCancel)
            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                DangerA11yButton(text = "Supprimer", onClick = onDelete)
            }
        }
    }
}

@Composable
private fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
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
            .then(if (selected) Modifier else Modifier.border(1.dp, A11yTokens.PaperBorder, shape))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = A11yTokens.minTouchHeight)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = if (selected) "$text, sélectionné" else text
            }
    )
}

@Composable
private fun CheckRow(checked: Boolean, label: String, onChecked: (Boolean) -> Unit) {
    // Checkbox avec onCheckedChange réel : le pattern toggleable+null
    // n’enregistre pas les taps sur One UI / uiautomator (case « Revient… » morte).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Checkbox) { onChecked(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
