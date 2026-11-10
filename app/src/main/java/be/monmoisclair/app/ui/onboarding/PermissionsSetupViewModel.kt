package be.monmoisclair.app.ui.onboarding

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.data.reminder.ReminderHealthReport
import be.monmoisclair.app.domain.usecase.CompleteOnboardingUseCase
import be.monmoisclair.app.ui.settings.SystemSettingsMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionsSetupUiState(
    val report: ReminderHealthReport? = null,
    val completing: Boolean = false,
    val completed: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PermissionsSetupViewModel @Inject constructor(
    private val healthChecker: ReminderHealthChecker,
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsSetupUiState())
    val uiState: StateFlow<PermissionsSetupUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val report = healthChecker.check()
            _uiState.update { it.copy(report = report, errorMessage = null) }
        }
    }

    fun intentIgnoreBattery(): Intent = healthChecker.intentIgnoreBatteryOptimizations()

    fun intentExactAlarm(): Intent = healthChecker.intentExactAlarmSettings()

    fun intentNotifications(): Intent = healthChecker.intentAppNotificationSettings()

    /** Réglages système inaccessibles (ROM / ActivityNotFound). */
    fun reportSettingsIntentFailed() {
        _uiState.update {
            it.copy(errorMessage = SystemSettingsMessages.OPEN_FAILED)
        }
    }

    fun finishOnboarding(allowContinueWithWarnings: Boolean) {
        viewModelScope.launch {
            val report = healthChecker.check()
            if (!allowContinueWithWarnings && !report.isFullyHealthy) {
                _uiState.update {
                    it.copy(
                        report = report,
                        errorMessage = "Activez encore les points marqués « À régler » pour des rappels fiables."
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(completing = true, errorMessage = null) }
            when (completeOnboarding()) {
                CompleteOnboardingUseCase.Result.Success ->
                    _uiState.update { it.copy(completing = false, completed = true) }
                CompleteOnboardingUseCase.Result.SafMissing ->
                    _uiState.update {
                        it.copy(
                            completing = false,
                            errorMessage = "Dossier de sauvegarde manquant. Revenez à l’étape précédente."
                        )
                    }
            }
        }
    }
}
