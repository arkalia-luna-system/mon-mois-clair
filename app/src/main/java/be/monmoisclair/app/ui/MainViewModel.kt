package be.monmoisclair.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.device.CredentialStorageGate
import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.summary.MonthSummaryPdfShare
import be.monmoisclair.app.domain.model.ThemePreference
import be.monmoisclair.app.ui.clipboard.SensitiveClipboard
import be.monmoisclair.app.domain.usecase.BiometricPreferenceUseCase
import be.monmoisclair.app.domain.usecase.ObserveOnboardingStateUseCase
import be.monmoisclair.app.domain.usecase.StartDestination
import be.monmoisclair.app.domain.usecase.ThemePreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MainUiState(
    val loading: Boolean = true,
    /** Téléphone pas encore déverrouillé post-boot — pas d’accès DB CE (Direct Boot). */
    val needsCredentialUnlock: Boolean = false,
    val startDestination: StartDestination = StartDestination.SafSetup,
    val biometricEnabled: Boolean = false,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unsupported,
    val unlocked: Boolean = true,
    val lockMessage: String? = null,
    val themePreference: ThemePreference = ThemePreference.System
) {
    /** Verrou affiché seulement si préféré + prêt matériellement + pas encore déverrouillé. */
    val needsUnlock: Boolean
        get() = biometricEnabled &&
            biometricAvailability == BiometricAvailability.Ready &&
            !unlocked
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val observeOnboarding: ObserveOnboardingStateUseCase,
    private val biometricPreference: BiometricPreferenceUseCase,
    private val themePreference: ThemePreferenceUseCase,
    private val receiptPhotos: ReceiptPhotoStore,
    val biometricAuth: BiometricAuthGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            themePreference = themePreference.preference.value,
            // Miroir sync : évite que LaunchedEffect FLAG_SECURE efface le flag posé au boot.
            biometricEnabled = biometricPreference.cachedEnabled()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themePreference.hydrate()
        }
        viewModelScope.launch {
            themePreference.preference.collect { pref ->
                _uiState.update { it.copy(themePreference = pref) }
            }
        }
        refreshStart()
    }

    fun refreshStart() {
        viewModelScope.launch {
            if (!CredentialStorageGate.isAvailable(appContext)) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        needsCredentialUnlock = true,
                        lockMessage = null
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(needsCredentialUnlock = false) }
            val dest = observeOnboarding.startDestination()
            val enabled = biometricPreference.isEnabled()
            val availability = biometricAuth.availability()
            // Si biométrie activée mais plus utilisable → ne pas bloquer l’accès (décision E).
            val mustLock = enabled && availability == BiometricAvailability.Ready
            _uiState.update {
                it.copy(
                    loading = false,
                    startDestination = dest,
                    biometricEnabled = enabled,
                    biometricAvailability = availability,
                    unlocked = !mustLock,
                    lockMessage = null,
                    themePreference = themePreference.preference.value
                )
            }
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
     * Verrouille d’abord de façon synchrone (état déjà connu) pour ne pas laisser
     * une fenêtre de contenu pendant le refresh prefs / disponibilité.
     */
    fun onAppBackgrounded() {
        _uiState.update { state ->
            if (state.biometricEnabled &&
                state.biometricAvailability == BiometricAvailability.Ready
            ) {
                state.copy(unlocked = false, lockMessage = null)
            } else {
                state
            }
        }
        viewModelScope.launch {
            val enabled = biometricPreference.isEnabled()
            val availability = biometricAuth.availability()
            val mustLock = enabled && availability == BiometricAvailability.Ready
            // Aperçu view/ + clipboard : wipe immédiat. share/ : purge TTL seulement
            // (wipe total cassait le FileProvider pendant le sheet PDF / .mmc).
            SensitiveClipboard.clearNow(appContext)
            withContext(Dispatchers.IO) {
                receiptPhotos.purgeViewCache()
                MonthSummaryPdfShare.purgeStale(MonthSummaryPdfShare.shareDir(appContext.cacheDir))
            }
            _uiState.update {
                it.copy(
                    biometricEnabled = enabled,
                    biometricAvailability = availability,
                    unlocked = !mustLock,
                    lockMessage = null
                )
            }
        }
    }
}
