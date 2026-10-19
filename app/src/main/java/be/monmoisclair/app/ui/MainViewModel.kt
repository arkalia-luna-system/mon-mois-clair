package be.monmoisclair.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.domain.usecase.BiometricPreferenceUseCase
import be.monmoisclair.app.domain.usecase.ObserveOnboardingStateUseCase
import be.monmoisclair.app.domain.usecase.StartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val loading: Boolean = true,
    val startDestination: StartDestination = StartDestination.SafSetup,
    val biometricEnabled: Boolean = false,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unsupported,
    val unlocked: Boolean = true,
    val lockMessage: String? = null
) {
    /** Verrou affiché seulement si préféré + prêt matériellement + pas encore déverrouillé. */
    val needsUnlock: Boolean
        get() = biometricEnabled &&
            biometricAvailability == BiometricAvailability.Ready &&
            !unlocked
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeOnboarding: ObserveOnboardingStateUseCase,
    private val biometricPreference: BiometricPreferenceUseCase,
    val biometricAuth: BiometricAuthGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshStart()
    }

    fun refreshStart() {
        viewModelScope.launch {
            val dest = observeOnboarding.startDestination()
            val enabled = biometricPreference.isEnabled()
            val availability = biometricAuth.availability()
            // Si biométrie activée mais plus utilisable → ne pas bloquer l’accès (décision E).
            val mustLock = enabled && availability == BiometricAvailability.Ready
            _uiState.value = MainUiState(
                loading = false,
                startDestination = dest,
                biometricEnabled = enabled,
                biometricAvailability = availability,
                unlocked = !mustLock,
                lockMessage = null
            )
        }
    }

    fun onUnlocked() {
        _uiState.update { it.copy(unlocked = true, lockMessage = null) }
    }

    fun setLockMessage(message: String?) {
        _uiState.update { it.copy(lockMessage = message) }
    }

    /**
     * Recharge le flag biométrie sans forcer un re-lock (ex. retour depuis Réglages).
     */
    fun reloadBiometricPreference() {
        viewModelScope.launch {
            val enabled = biometricPreference.isEnabled()
            val availability = biometricAuth.availability()
            _uiState.update {
                it.copy(
                    biometricEnabled = enabled,
                    biometricAvailability = availability
                )
            }
        }
    }

    /**
     * Re-verrouille quand l’activité quitte l’avant-plan (pas lors d’une rotation).
     */
    fun onAppBackgrounded() {
        viewModelScope.launch {
            val enabled = biometricPreference.isEnabled()
            val availability = biometricAuth.availability()
            val mustLock = enabled && availability == BiometricAvailability.Ready
            _uiState.update {
                it.copy(
                    biometricEnabled = enabled,
                    biometricAvailability = availability,
                    unlocked = if (mustLock) false else true,
                    lockMessage = null
                )
            }
        }
    }
}
