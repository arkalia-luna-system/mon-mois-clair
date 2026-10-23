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
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
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
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import java.io.File

/** Sélecteur PDF (courrier mail) — ajout et édition, pas sur l’accueil. */
@Composable
private fun rememberPdfPicker(
    onBytes: (ByteArray) -> Unit,
    onFailed: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                be.monmoisclair.app.data.backup.BackupSecurityArchive.readBounded(
                    it,
                    ReceiptPhotoStore.MAX_PDF_BYTES.toLong()
                )
            } ?: throw BackupFailure.Io("Impossible de lire le PDF")
            if (!ReceiptPhotoStore.looksLikePdf(bytes)) {
                throw BackupFailure.Io("Ce fichier n’est pas un PDF lisible.")
            }
            onBytes(bytes)
        }.onFailure { e ->
            val raw = e.message.orEmpty()
            val shown = if (raw.contains("trop lourde") || raw.contains("trop lourd")) {
                "Ce courrier est trop lourd."
            } else {
                raw.ifBlank { "Ce PDF n’a pas pu être lu. Réessayez." }
            }
            onFailed(shown)
        }
    }
    return { launcher.launch(arrayOf("application/pdf")) }
}

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

/** Évite OOM sur un JPEG caméra très lourd (borne ~2048 px) + EXIF Samsung. */
private fun decodeCameraJpegBounded(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (longest / sample > ReceiptPhotoStore.MAX_SIDE_PX) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    val decoded = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
    return ReceiptPhotoStore.applyExifOrientation(decoded, file)
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
    val pickPdf = rememberPdfPicker(
        onBytes = { bytes ->
            viewModel.attachPendingPdf(bytes)
        },
        onFailed = viewModel::setPdfReadError
    )
    var showReceiptDialog by remember { mutableStateOf(false) }
    // Déplier « retard ou accord » sans rien saisir ≠ sale (C6 / Senior :
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
        hasPendingPdf = state.hasPendingPdf,
        receiptPreview = pendingPreview,
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
        },
        ocrBusy = state.ocrBusy,
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
    val scope = rememberCoroutineScope()
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
            state.hasPendingPdf ||
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
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperCream,
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
        if (!ok) viewModel.setPdfReadError("Aucune app pour ouvrir ce PDF.")
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
                            val photoErrShape = MaterialTheme.shapes.medium
                            val photoErr =
                                "Impossible d’afficher cette photo ici. " +
                                    "Essayez « Ouvrir à part », ou reprenez-en une."
                            Text(
                                text = photoErr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.DangerInk,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        A11yTokens.DangerInk.copy(alpha = 0.4f),
                                        photoErrShape
                                    )
                                    .background(A11yTokens.PaperWarm, photoErrShape)
                                    .padding(12.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                        contentDescription = photoErr
                                    }
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
        hasPendingPdf = state.hasPendingPdf,
        hasReceiptPhoto = state.receiptRelativePath.isNotBlank(),
        receiptIsPdf = state.receiptRelativePath.isNotBlank() &&
            viewModel.isPdfPath(state.receiptRelativePath),
        receiptPreview = pendingPreview ?: receiptBitmap,
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
        extraReceiptPaths = ReceiptPathList.parse(state.extraReceiptPaths),
        isPdfPath = viewModel::isPdfPath,
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
        },
        ocrBusy = state.ocrBusy,
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
        primaryEnabled = !state.saving && !state.futurePreviewReadOnly,
        deleteLabel = if (state.isSavingsLine) "Retirer la ligne" else "Supprimer",
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = {},
        onRecurring = {},
        onApplyFollowing = viewModel::setApplyToFollowing,
        onPrimary = viewModel::save,
        onCancel = { requestLeave() },
        onDelete = {
            if (!state.saving && !state.futurePreviewReadOnly) confirmDelete = true
        }
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
    val formGap = if (fit.compactHomeChrome) 8.dp else 12.dp
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
            // Sous sticky Enregistrer : jours 25–31 scrollables / tappables (S25 smoke).
            Spacer(
                modifier = Modifier.height(
                    A11yTokens.minTouchHeight + A11yTokens.bottomSystemClearance
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
                        if (onCopyPaymentNote != null && paymentNote.isNotBlank()) {
                            QuietLinkA11y(
                                text = "Copier la communication",
                                onClick = onCopyPaymentNote
                            )
                        }
                        if (onReadCommFromPhoto != null) {
                            QuietLinkA11y(
                                text = if (ocrBusy) "Lecture de la photo…" else "Lire +++ sur la photo",
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
                                text = "Ranger sans plan",
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Annuler",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (primaryEnabled) A11yTokens.Link else A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            enabled = primaryEnabled,
                            role = Role.Button,
                            onClickLabel = "Annuler sans enregistrer",
                            onClick = onCancel
                        )
                        .padding(vertical = 12.dp)
                        .semantics { contentDescription = "Annuler" }
                )
                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))
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

/** Titre de section repliable — titleLarge, pas un Secondary plein largeur. */
@Composable
private fun FormSectionToggle(
    title: String,
    expanded: Boolean,
    closedHint: String,
    onToggle: () -> Unit
) {
    val label = if (expanded) "$title ▴" else "$title ▾"
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) "Replier $title" else "Déplier $title",
                    onClick = onToggle
                )
                .padding(vertical = 10.dp)
                .semantics { contentDescription = label }
        )
        // Fermé : hint utile. Ouvert : le ▴ suffit (pas de « Ouvert » redondant).
        if (!expanded) {
            Text(
                text = closedHint,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
    }
}

@Composable
private fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val fit = rememberDisplayFit()
    val hPad = if (fit.compactHomeChrome) 12.dp else 16.dp
    val vPad = if (fit.compactHomeChrome) 10.dp else 12.dp
    // FORM02 : choix exclusifs = pastille capsule, pas bouton 16 dp.
    val shape = StampShape
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
    // Pas de carte bordée : rythme crème + case teal (FORM07).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Checkbox) { onChecked(!checked) }
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = if (checked) "$label, coché" else "$label, non coché"
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FORM07 : case teal carnet (pas Material stock).
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = A11yTokens.ButtonFill,
                uncheckedColor = A11yTokens.FieldLine,
                checkmarkColor = A11yTokens.ButtonText
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.Ink
        )
    }
}
