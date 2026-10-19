package be.monmoisclair.app.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAuthResult
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.domain.usecase.BiometricPreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val loading: Boolean = true,
    val biometricEnabled: Boolean = false,
    val availability: BiometricAvailability = BiometricAvailability.Unsupported,
    val showDeviceCareSection: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val busy: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preference: BiometricPreferenceUseCase,
    val biometricAuth: BiometricAuthGateway,
    private val healthChecker: ReminderHealthChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = false,
                    biometricEnabled = preference.isEnabled(),
                    availability = biometricAuth.availability(),
                    showDeviceCareSection = healthChecker.isSamsungDevice(),
                    message = null,
                    isError = false
                )
            }
        }
    }

    fun intentBatteryList(): Intent = healthChecker.intentBatteryOptimizationList()

    fun intentAppDetails(): Intent = healthChecker.intentApplicationDetails()

    fun onEnableAuthResult(result: BiometricAuthResult) {
        viewModelScope.launch {
            when (result) {
                BiometricAuthResult.Success -> {
                    preference.setEnabled(true)
                    _uiState.update {
                        it.copy(
                            biometricEnabled = true,
                            busy = false,
                            message = "Verrouillage activé. Au prochain retour dans l’app, " +
                                "empreinte ou code d’écran seront demandés.",
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
        _uiState.update {
            it.copy(
                message = "Impossible d’ouvrir cet écran système. Réessayez depuis les réglages Samsung.",
                isError = true
            )
        }
    }
}
