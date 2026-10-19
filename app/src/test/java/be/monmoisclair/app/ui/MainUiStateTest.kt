package be.monmoisclair.app.ui

import be.monmoisclair.app.data.biometric.BiometricAvailability
import be.monmoisclair.app.domain.usecase.StartDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MainUiStateTest {

    @Test
    fun needsUnlock_only_when_enabled_ready_and_locked() {
        val base = MainUiState(
            loading = false,
            startDestination = StartDestination.Home,
            biometricEnabled = true,
            biometricAvailability = BiometricAvailability.Ready,
            unlocked = false
        )
        assertThat(base.needsUnlock).isTrue()
        assertThat(base.copy(unlocked = true).needsUnlock).isFalse()
        assertThat(base.copy(biometricEnabled = false).needsUnlock).isFalse()
        assertThat(
            base.copy(biometricAvailability = BiometricAvailability.NoneEnrolled).needsUnlock
        ).isFalse()
    }

    @Test
    fun session_overlay_rule_documented() {
        // Règle UI (MainActivity) : cold start verrouillé = lock seul ;
        // après 1er unlock, NavHost reste monté sous overlay pour ne pas perdre
        // Réglages / Santé quand on ouvre un écran système.
        val locked = MainUiState(
            biometricEnabled = true,
            biometricAvailability = BiometricAvailability.Ready,
            unlocked = false
        )
        assertThat(locked.needsUnlock).isTrue()
    }
}
