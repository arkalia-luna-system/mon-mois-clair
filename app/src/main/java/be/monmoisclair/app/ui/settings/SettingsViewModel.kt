package be.monmoisclair.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import be.monmoisclair.app.data.backup.BackupHealthStore
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.data.crypto.DbPassphraseVault
import be.monmoisclair.app.data.crypto.ShareSecretVault
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.data.summary.MonthSummaryPdfShare
import be.monmoisclair.app.data.summary.MonthSummaryPdfWriter
import be.monmoisclair.app.domain.ShareFileIntents
import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.domain.model.ThemePreference
import be.monmoisclair.app.domain.usecase.BiometricPreferenceUseCase
import be.monmoisclair.app.domain.usecase.ConfigureSavingsResult
import be.monmoisclair.app.domain.usecase.ConfigureSavingsUseCase
import be.monmoisclair.app.domain.usecase.ListDossiersUseCase
import be.monmoisclair.app.domain.usecase.ListOffMonthPeriodicsUseCase
import be.monmoisclair.app.domain.usecase.OffMonthPeriodic
import be.monmoisclair.app.domain.usecase.PrepareMonthSummaryUseCase
import be.monmoisclair.app.domain.usecase.SavingsAsideState
import be.monmoisclair.app.domain.usecase.ShareBackupResult
import be.monmoisclair.app.domain.usecase.ShareBackupUseCase
import be.monmoisclair.app.domain.usecase.ThemePreferenceUseCase
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val loading: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.System,
    val biometricEnabled: Boolean = false,
    val availability: BiometricAvailability = BiometricAvailability.Unsupported,
    val backupHealth: BackupHealth = BackupHealth.NOT_CONFIGURED,
    val offMonthPeriodics: List<OffMonthPeriodic> = emptyList(),
    /** Feedback Confidentialité (biométrie) uniquement. */
    val message: String? = null,
    val isError: Boolean = false,
    /** Feedback carte Sauvegarde (partage / erreur intent). */
    val backupMessage: String? = null,
    val backupMessageError: Boolean = false,
    /** Erreur intent hors sauvegarde (résumé, mail support) — sous le titre. */
    val flashMessage: String? = null,
    val flashMessageError: Boolean = false,
    val busy: Boolean = false,
    val savingsAmountText: String = "",
    val savingsDay: Int = 1,
    val savingsActive: Boolean = false,
    val savingsDeficit: String? = null,
    /** Ce mois est déjà viré : le montant saisi attend le mois suivant. */
    val savingsNextMonthNote: String? = null,
    val savingsBusy: Boolean = false,
    val savingsMessage: String? = null,
    val savingsMessageError: Boolean = false,
    /** Lot F — visible si déficit ou au moins un dossier. */
    val showSummaryButton: Boolean = false,
    /** QuietLink « Aussi dans Dossiers » seulement si un dossier existe (aligné Menu). */
    val showDossiersSummaryLink: Boolean = false,
    val summaryBusy: Boolean = false,
    /** Code de copie (8 chiffres) pour rouvrir une copie WhatsApp / autre téléphone. */
    val shareCopyCode: String? = null,
    /** Migration SQLCipher Failed → DB claire temporaire (bannière confiance). */
    val dbMigrationFailed: Boolean = false,
    /** Aligné Menu Accueil : hub Alertes en warn si bloquées. */
    val remindersBlocked: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val preference: BiometricPreferenceUseCase,
    private val themePreference: ThemePreferenceUseCase,
    val biometricAuth: BiometricAuthGateway,
    private val healthStore: BackupHealthStore,
    private val listOffMonth: ListOffMonthPeriodicsUseCase,
    private val shareBackup: ShareBackupUseCase,
    private val configureSavings: ConfigureSavingsUseCase,
    private val prepareMonthSummary: PrepareMonthSummaryUseCase,
    private val listDossiers: ListDossiersUseCase,
    private val summaryPdfWriter: MonthSummaryPdfWriter,
    private val shareSecretVault: ShareSecretVault,
    private val dbPassphraseVault: DbPassphraseVault,
    private val reminderHealth: ReminderHealthChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _shareIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val shareIntents: SharedFlow<Intent> = _shareIntents.asSharedFlow()
    private var savingsPreviewJob: Job? = null
    /** Saisie en cours : un retour dans l’écran ne doit pas l’effacer. */
    private var savingsDraftDirty = false
    /** Dernier intent lancé — pour router l’erreur startActivity. */
    private var pendingShareKind: PendingShareKind = PendingShareKind.None

    private enum class PendingShareKind { None, Backup, Other }

    init {
        refresh()
    }

    fun refresh() {
        val dirtyAtStart = savingsDraftDirty
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                val savings = configureSavings.read()
                val summary = prepareMonthSummary()
                val dossierCount = listDossiers().count
                val offMonth = listOffMonth()
                val code = runCatching { shareSecretVault.peekDisplayCode() }.getOrNull()
                val failed = runCatching { dbPassphraseVault.isMigrationFailedPresent() }
                    .getOrDefault(false)
                val alertsBlocked = reminderHealth.check().remindersBlocked
                RefreshBundle(savings, summary, dossierCount, offMonth, code, failed, alertsBlocked)
            }
            _uiState.update { cur ->
                val dirtyNow = savingsDraftDirty
                val keepDraft = dirtyNow || cur.savingsBusy || dirtyAtStart != dirtyNow
                val savings = loaded.savings
                val storedAmount = if (savings.active) {
                    MoneyFormat.centsToEditable(savings.amountCents)
                } else {
                    ""
                }
                // Ne pas effacer message / backupMessage / flashMessage :
                // ON_RESUME après le share sheet doit encore montrer « Choisissez l’app… ».
                cur.copy(
                    loading = false,
                    themePreference = themePreference.read(),
                    biometricEnabled = preference.isEnabled(),
                    availability = biometricAuth.availability(),
                    backupHealth = healthStore.read(),
                    offMonthPeriodics = loaded.offMonth,
                    savingsActive = savings.active,
                    savingsAmountText = if (keepDraft) cur.savingsAmountText else storedAmount,
                    savingsDay = if (keepDraft) {
                        cur.savingsDay
                    } else {
                        savings.dayOfMonth.coerceIn(1, 31)
                    },
                    savingsDeficit = if (keepDraft) cur.savingsDeficit else null,
                    savingsNextMonthNote = if (keepDraft) cur.savingsNextMonthNote else null,
                    showSummaryButton = loaded.summary.isDeficit || loaded.dossierCount > 0,
                    showDossiersSummaryLink = loaded.dossierCount > 0,
                    shareCopyCode = loaded.shareCode,
                    dbMigrationFailed = loaded.migrationFailed,
                    remindersBlocked = loaded.remindersBlocked
                )
            }
        }
    }

    fun setThemePreference(preference: ThemePreference) {
        if (_uiState.value.themePreference == preference) return
        viewModelScope.launch {
            themePreference.set(preference)
            _uiState.update { it.copy(themePreference = preference) }
        }
    }

    fun setSavingsAmount(value: String) {
        savingsDraftDirty = true
        _uiState.update {
            it.copy(
                savingsAmountText = value,
                savingsDeficit = null,
                savingsNextMonthNote = null,
                savingsMessage = null,
                savingsMessageError = false
            )
        }
        savingsPreviewJob?.cancel()
        savingsPreviewJob = viewModelScope.launch {
            if (MoneyFormat.tooManyFractionDigits(value)) return@launch
            val cents = MoneyFormat.parseToCents(value) ?: return@launch
            if (cents <= 0L) return@launch
            val preview = configureSavings.previewAmount(cents)
            val deficit = if (!preview.alreadyTransferredThisMonth && preview.remainingCents < 0L) {
                "Ce mois passerait en déficit de ${MoneyFormat.formatCents(-preview.remainingCents)}. " +
                    "Vous pouvez quand même enregistrer."
            } else {
                null
            }
            val nextNote = if (preview.alreadyTransferredThisMonth) {
                "Ce mois est déjà FAIT. Le nouveau montant commence le mois suivant."
            } else {
                null
            }
            _uiState.update { cur ->
                if (cur.savingsAmountText != value) {
                    cur
                } else {
                    cur.copy(savingsDeficit = deficit, savingsNextMonthNote = nextNote)
                }
            }
        }
    }

    fun setSavingsDay(day: Int) {
        if (day !in 1..31) return
        savingsDraftDirty = true
        _uiState.update { it.copy(savingsDay = day) }
    }

    fun saveSavings() {
        val s = _uiState.value
        if (s.busy || s.savingsBusy) return
        if (MoneyFormat.tooManyFractionDigits(s.savingsAmountText)) {
            _uiState.update {
                it.copy(savingsMessage = MoneyFormat.FRACTION_HINT, savingsMessageError = true)
            }
            return
        }
        val cents = MoneyFormat.parseToCents(s.savingsAmountText)
        if (cents == null || cents <= 0L) {
            _uiState.update {
                it.copy(
                    savingsMessage = "Indiquez un montant à mettre de côté.",
                    savingsMessageError = true
                )
            }
            return
        }
        if (MoneyFormat.isOverLineMax(cents)) {
            _uiState.update {
                it.copy(
                    savingsMessage = MoneyFormat.LINE_MAX_HINT,
                    savingsMessageError = true
                )
            }
            return
        }
        _uiState.update {
            it.copy(savingsBusy = true, savingsMessage = null, savingsMessageError = false)
        }
        viewModelScope.launch {
            when (val result = configureSavings.save(cents, s.savingsDay)) {
                ConfigureSavingsResult.Saved -> {
                    val again = configureSavings.read()
                    val startsNextMonth = configureSavings.previewAmount(cents)
                        .alreadyTransferredThisMonth
                    savingsDraftDirty = false
                    val noted = if (startsNextMonth) {
                        "Noté — nouveau montant dès le mois suivant."
                    } else {
                        "Noté."
                    }
                    _uiState.update {
                        it.copy(
                            savingsBusy = false,
                            savingsActive = again.active,
                            savingsAmountText = if (again.active) {
                                MoneyFormat.centsToEditable(again.amountCents)
                            } else {
                                s.savingsAmountText
                            },
                            savingsDay = if (again.active) again.dayOfMonth else s.savingsDay,
                            savingsDeficit = null,
                            savingsNextMonthNote = if (startsNextMonth) {
                                "Ce mois est déjà FAIT. Le nouveau montant commence le mois suivant."
                            } else {
                                null
                            },
                            savingsMessage = noted,
                            savingsMessageError = false
                        )
                    }
                }
                is ConfigureSavingsResult.InvalidInput -> {
                    _uiState.update {
                        it.copy(
                            savingsBusy = false,
                            savingsMessage = result.message,
                            savingsMessageError = true
                        )
                    }
                }
                ConfigureSavingsResult.Removed -> {
                    _uiState.update { it.copy(savingsBusy = false) }
                }
            }
        }
    }

    fun removeSavings() {
        if (_uiState.value.busy || _uiState.value.savingsBusy) return
        _uiState.update {
            it.copy(savingsBusy = true, savingsMessage = null, savingsMessageError = false)
        }
        viewModelScope.launch {
            when (val result = configureSavings.remove()) {
                ConfigureSavingsResult.Removed -> {
                    savingsDraftDirty = false
                    _uiState.update {
                        it.copy(
                            savingsBusy = false,
                            savingsActive = false,
                            savingsAmountText = "",
                            savingsDay = 1,
                            savingsDeficit = null,
                            savingsNextMonthNote = null,
                            savingsMessage = "Ligne retirée. Les virements déjà marqués FAIT restent.",
                            savingsMessageError = false
                        )
                    }
                }
                is ConfigureSavingsResult.InvalidInput -> {
                    _uiState.update {
                        it.copy(
                            savingsBusy = false,
                            savingsMessage = result.message,
                            savingsMessageError = true
                        )
                    }
                }
                ConfigureSavingsResult.Saved -> {
                    _uiState.update { it.copy(savingsBusy = false) }
                }
            }
        }
    }

    /** Lot F — même texte que Mes dossiers (WhatsApp / Mail). */
    fun shareMonthSummary() {
        if (_uiState.value.busy || _uiState.value.summaryBusy) return
        pendingShareKind = PendingShareKind.Other
        _uiState.update {
            it.copy(summaryBusy = true, flashMessage = null, flashMessageError = false)
        }
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { prepareMonthSummary() }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Résumé — Mon Mois Clair")
                    putExtra(Intent.EXTRA_TEXT, result.text)
                }
                _shareIntents.emit(
                    Intent.createChooser(intent, "Envoyer le résumé")
                )
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        flashMessage = "Le résumé n’a pas pu être préparé. Réessayez.",
                        flashMessageError = true
                    )
                }
            } finally {
                _uiState.update { it.copy(summaryBusy = false) }
            }
        }
    }

    /** Même texte en PDF (imprimer / joindre). */
    fun shareMonthSummaryPdf() {
        if (_uiState.value.busy || _uiState.value.summaryBusy) return
        pendingShareKind = PendingShareKind.Other
        _uiState.update {
            it.copy(summaryBusy = true, flashMessage = null, flashMessageError = false)
        }
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val result = prepareMonthSummary()
                    // PDF clair éphémère (cache/share) — TTL 2 min ; pas files/ durable.
                    val out = MonthSummaryPdfShare.prepareTarget(appContext.cacheDir)
                    summaryPdfWriter.write(result.text, out)
                }
                val uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    file
                )
                val intent = ShareFileIntents.pdf(
                    uri,
                    "Résumé — Mon Mois Clair",
                    text = ShareFileIntents.PDF_SUMMARY_SHARE_NOTE
                )
                _shareIntents.emit(
                    Intent.createChooser(intent, "Envoyer le PDF")
                )
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        flashMessage = "Le PDF n’a pas pu être préparé. Réessayez.",
                        flashMessageError = true
                    )
                }
            } finally {
                _uiState.update { it.copy(summaryBusy = false) }
            }
        }
    }

    fun shareSecurityCopy() {
        if (_uiState.value.busy || _uiState.value.savingsBusy) return
        pendingShareKind = PendingShareKind.Backup
        _uiState.update {
            it.copy(busy = true, backupMessage = null, backupMessageError = false)
        }
        viewModelScope.launch {
            when (val result = shareBackup()) {
                is ShareBackupResult.Ready -> {
                    _shareIntents.emit(result.intent)
                    _uiState.update {
                        it.copy(
                            busy = false,
                            backupMessage = "Choisissez l’app d’envoi. " +
                                "Gardez le code ci-dessus sur papier — " +
                                "ne l’envoyez pas dans le même message que la copie.",
                            backupMessageError = false
                        )
                    }
                }
                is ShareBackupResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            busy = false,
                            backupMessage = result.message,
                            backupMessageError = true
                        )
                    }
                }
            }
        }
    }

    fun formatPeriodicAmountLine(item: OffMonthPeriodic): String =
        OffMonthPeriodicFormat.amountLine(item)

    fun onEnableAuthResult(result: BiometricAuthResult) {
        viewModelScope.launch {
            when (result) {
                BiometricAuthResult.Success -> {
                    preference.setEnabled(true)
                    _uiState.update {
                        it.copy(
                            biometricEnabled = true,
                            busy = false,
                            message = "Verrouillage activé. Empreinte ou code liés à la protection du carnet. " +
                                "Les montants des alertes et du widget sont masqués.",
                            isError = false
                        )
                    }
                }
                BiometricAuthResult.Cancelled -> {
                    _uiState.update {
                        it.copy(
                            busy = false,
                            biometricEnabled = false,
                            message = null,
                            isError = false
                        )
                    }
                }
                is BiometricAuthResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            busy = false,
                            biometricEnabled = false,
                            message = result.message,
                            isError = true
                        )
                    }
                }
            }
        }
    }

    fun disableBiometric() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            preference.setEnabled(false)
            _uiState.update {
                it.copy(
                    busy = false,
                    biometricEnabled = false,
                    message = "Verrouillage désactivé. L’app s’ouvre directement.",
                    isError = false
                )
            }
        }
    }

    fun markBusyForPrompt() {
        _uiState.update { it.copy(busy = true, message = null, isError = false) }
    }

    fun reportExternalIntentFailed() {
        val msg = "Impossible d’ouvrir l’écran système. Réessayez."
        when (pendingShareKind) {
            PendingShareKind.Backup -> {
                _uiState.update {
                    it.copy(
                        busy = false,
                        backupMessage = msg,
                        backupMessageError = true
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        busy = false,
                        summaryBusy = false,
                        flashMessage = msg,
                        flashMessageError = true
                    )
                }
            }
        }
        pendingShareKind = PendingShareKind.None
    }

    private data class RefreshBundle(
        val savings: SavingsAsideState,
        val summary: PrepareMonthSummaryUseCase.Result,
        val dossierCount: Int,
        val offMonth: List<OffMonthPeriodic>,
        val shareCode: String?,
        val migrationFailed: Boolean,
        val remindersBlocked: Boolean
    )
}
