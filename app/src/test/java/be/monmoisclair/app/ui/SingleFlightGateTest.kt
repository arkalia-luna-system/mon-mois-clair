package be.monmoisclair.app.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SingleFlightGateTest {

    @Test
    fun rejectsParallelUntilExit() {
        val gate = SingleFlightGate()
        assertThat(gate.tryEnter()).isTrue()
        assertThat(gate.tryEnter()).isFalse()
        gate.exit()
        assertThat(gate.tryEnter()).isTrue()
        gate.exit()
    }
}
