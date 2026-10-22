package be.monmoisclair.app.ui.dossiers

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import be.monmoisclair.app.data.summary.MonthSummaryPdfWriter
import be.monmoisclair.app.domain.ShareFileIntents
import be.monmoisclair.app.domain.usecase.DossiersSnapshot
import be.monmoisclair.app.domain.usecase.ListDossiersUseCase
import be.monmoisclair.app.domain.usecase.ListPendingLettersUseCase
import be.monmoisclair.app.domain.usecase.PrepareMonthSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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
    val summaryBusy: Boolean = false
)

@HiltViewModel
class DossiersViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val listDossiers: ListDossiersUseCase,
    private val listPendingLetters: ListPendingLettersUseCase,
    private val prepareMonthSummary: PrepareMonthSummaryUseCase,
    private val summaryPdfWriter: MonthSummaryPdfWriter
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
        _state.value = _state.value.copy(summaryBusy = true)
        viewModelScope.launch {
            val result = prepareMonthSummary()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Résumé — Mon Mois Clair")
                putExtra(Intent.EXTRA_TEXT, result.text)
            }
            _shareIntents.emit(
                Intent.createChooser(intent, "Envoyer le résumé")
            )
            _state.value = _state.value.copy(summaryBusy = false)
        }
    }

    /** Même texte en PDF (imprimer / joindre). */
    fun shareSummaryPdf() {
        if (_state.value.summaryBusy || _state.value.loading) return
        _state.value = _state.value.copy(summaryBusy = true)
        viewModelScope.launch {
            val result = prepareMonthSummary()
            val file = withContext(Dispatchers.IO) {
                val dir = File(appContext.cacheDir, "share").also { it.mkdirs() }
                val out = File(dir, MonthSummaryPdfWriter.FILE_NAME)
                summaryPdfWriter.write(result.text, out)
            }
            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
            val intent = ShareFileIntents.pdf(uri, "Résumé — Mon Mois Clair")
            _shareIntents.emit(
                Intent.createChooser(intent, "Envoyer le PDF")
            )
            _state.value = _state.value.copy(summaryBusy = false)
        }
    }
}
