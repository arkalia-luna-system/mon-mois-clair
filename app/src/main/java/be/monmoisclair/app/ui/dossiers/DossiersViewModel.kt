package be.monmoisclair.app.ui.dossiers

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.data.summary.MonthSummaryPdfShare
import be.monmoisclair.app.data.summary.MonthSummaryPdfWriter
import be.monmoisclair.app.domain.ShareFileIntents
import be.monmoisclair.app.domain.usecase.DossiersSnapshot
import be.monmoisclair.app.domain.usecase.ListDossiersUseCase
import be.monmoisclair.app.domain.usecase.ListPendingLettersUseCase
import be.monmoisclair.app.domain.usecase.PrepareMonthSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

data class DossiersUiState(
    val loading: Boolean = true,
    val snapshot: DossiersSnapshot = DossiersSnapshot(emptyList(), 0L),
    val pendingLetters: List<PendingLetterEntity> = emptyList(),
    val summaryBusy: Boolean = false,
    val summaryMessage: String? = null,
    val summaryMessageError: Boolean = false
)

@HiltViewModel
class DossiersViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val listDossiers: ListDossiersUseCase,
    private val listPendingLetters: ListPendingLettersUseCase,
    private val prepareMonthSummary: PrepareMonthSummaryUseCase,
    private val summaryPdfWriter: MonthSummaryPdfWriter,
    private val pendingLetterDao: PendingLetterDao,
    private val mutatingRepository: MutatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DossiersUiState())
    val state: StateFlow<DossiersUiState> = _state.asStateFlow()

    private val _shareIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val shareIntents: SharedFlow<Intent> = _shareIntents.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _state.value
            val firstPaint =
                previous.snapshot.items.isEmpty() && previous.pendingLetters.isEmpty()
            // Ne pas vider la liste (spinner plein écran) à chaque ON_RESUME :
            // sinon micro-saut quand on revient d’une fiche / du multitâche.
            if (firstPaint) {
                _state.update { it.copy(loading = true) }
            }
            val snap = listDossiers()
            val pending = listPendingLetters()
            _state.update {
                it.copy(
                    loading = false,
                    snapshot = snap,
                    pendingLetters = pending
                )
            }
        }
    }

    /** Lot F — texte à envoyer à un créancier ou à un conseiller. */
    fun shareSummary() {
        if (_state.value.summaryBusy || _state.value.loading) return
        _state.update {
            it.copy(summaryBusy = true, summaryMessage = null, summaryMessageError = false)
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
                _state.update {
                    it.copy(
                        summaryMessage = "Le résumé n’a pas pu être préparé. Réessayez.",
                        summaryMessageError = true
                    )
                }
            } finally {
                _state.update { it.copy(summaryBusy = false) }
            }
        }
    }

    /** Même texte en PDF (imprimer / joindre). */
    fun shareSummaryPdf() {
        if (_state.value.summaryBusy || _state.value.loading) return
        _state.update {
            it.copy(summaryBusy = true, summaryMessage = null, summaryMessageError = false)
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
                _state.update {
                    it.copy(
                        summaryMessage = "Le PDF n’a pas pu être préparé. Réessayez.",
                        summaryMessageError = true
                    )
                }
            } finally {
                _state.update { it.copy(summaryBusy = false) }
            }
        }
    }

    fun reportShareIntentFailed() {
        _state.update {
            it.copy(
                summaryMessage = "Aucune app pour envoyer. Réessayez.",
                summaryMessageError = true
            )
        }
    }

    /** Retire un courrier saisi par erreur (pas encore converti en accord). */
    fun discardPendingLetter(id: String) {
        viewModelScope.launch {
            mutatingRepository.mutate(rescheduleReminders = false) {
                pendingLetterDao.deleteById(id)
            }
            refresh()
        }
    }
}
