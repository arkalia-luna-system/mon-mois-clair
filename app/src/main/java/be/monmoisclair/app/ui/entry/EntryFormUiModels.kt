package be.monmoisclair.app.ui.entry

import android.graphics.Bitmap
import androidx.compose.ui.Modifier

/**
 * Modèles UI immuables pour [EntryFormScaffold] — remplace la liste plate ~82 params (A-P0-1).
 * Callbacks groupés par section ; Add/Edit construisent ces objets, le Scaffold ne les étale plus.
 */
data class EntryFormIdentityUi(
    val title: String,
    val label: String,
    val amountText: String,
    val dayText: String,
    val isExpense: Boolean,
    val onLabel: (String) -> Unit,
    val onAmount: (String) -> Unit,
    val onDay: (String) -> Unit,
    val onExpense: (Boolean) -> Unit
)

data class EntryFormRecurrenceUi(
    val recurring: Boolean,
    val showRecurring: Boolean,
    val recurrence: RecurrenceChoice = RecurrenceChoice.Once,
    val onRecurrence: ((RecurrenceChoice) -> Unit)? = null,
    val showFrequencyEdit: Boolean = false,
    val recurrenceInfo: String? = null,
    val nextDueMonth: Int = 1,
    val onNextDueMonth: (Int) -> Unit = {},
    val showNextDueMonth: Boolean = false,
    val onRecurring: (Boolean) -> Unit,
    val showApplyFollowing: Boolean = false,
    val applyFollowing: Boolean = false,
    val hasTemplate: Boolean = false,
    val onApplyFollowing: (Boolean) -> Unit = {}
)

data class EntryFormPaymentUi(
    val isAutoDebit: Boolean = false,
    val onAutoDebit: (Boolean) -> Unit = {},
    val showAutoDebit: Boolean = false,
    val paymentNote: String = "",
    val onPaymentNote: (String) -> Unit = {},
    val showPaymentAssist: Boolean = false,
    val hasPendingPhoto: Boolean = false,
    val hasPendingPdf: Boolean = false,
    val hasReceiptPhoto: Boolean = false,
    val receiptIsPdf: Boolean = false,
    val receiptPreview: Bitmap? = null,
    val extraReceiptPaths: List<String> = emptyList(),
    val isPdfPath: (String) -> Boolean = { false },
    val ocrBusy: Boolean = false,
    val onScanEpc: (() -> Unit)? = null,
    val onTakePhoto: (() -> Unit)? = null,
    val onPickPdf: (() -> Unit)? = null,
    val onClearPendingPdf: (() -> Unit)? = null,
    val onClearReceipt: (() -> Unit)? = null,
    val onViewReceipt: (() -> Unit)? = null,
    val onViewExtraReceipt: ((String) -> Unit)? = null,
    val onRemoveExtraReceipt: ((String) -> Unit)? = null,
    val onCopyPaymentNote: (() -> Unit)? = null,
    val onReadCommFromPhoto: (() -> Unit)? = null
)

data class EntryFormDossierUi(
    val showDossierSection: Boolean = false,
    val dossierExpanded: Boolean = false,
    val onDossierExpanded: (Boolean) -> Unit = {},
    val causeLabel: String = "",
    val onCauseLabel: (String) -> Unit = {},
    val openingRemainingText: String = "",
    val onOpeningRemainingText: (String) -> Unit = {},
    val isAgreement: Boolean = false,
    val remainingCents: Long? = null,
    val isSettled: Boolean = false,
    val pacePhrase: String? = null,
    val iban: String = "",
    val ibanBring: Modifier = Modifier,
    val onIban: (String) -> Unit = {},
    val onCopyIban: (() -> Unit)? = null,
    val creditorEmail: String = "",
    val creditorEmailBring: Modifier = Modifier,
    val onCreditorEmail: (String) -> Unit = {},
    val onWriteMail: (() -> Unit)? = null,
    val onSaveAsPendingLetter: (() -> Unit)? = null,
    val feeAddText: String = "",
    val feeAddBring: Modifier = Modifier,
    val onFeeAddText: (String) -> Unit = {},
    val feeNoteText: String = "",
    val feeNoteBring: Modifier = Modifier,
    val onFeeNoteText: (String) -> Unit = {},
    val addedFeesCents: Long = 0L,
    val feeEvents: List<FeeEventRow> = emptyList()
)

data class EntryFormSavingsUi(
    val isSavingsLine: Boolean = false,
    val savingsTransferredCents: Long = 0L,
    val lockAmountAndDay: Boolean = false
)

data class EntryFormChromeUi(
    val infoMessage: String? = null,
    val error: String?,
    val primaryLabel: String,
    val primaryEnabled: Boolean,
    val deleteLabel: String = "Supprimer",
    val onPrimary: () -> Unit,
    val onCancel: () -> Unit,
    val onDelete: (() -> Unit)? = null
)
