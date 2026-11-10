package be.monmoisclair.app.ui.scan

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class EpcScanHandoffTest {

    @Before
    fun clear() {
        EpcScanHandoff.take()
    }

    @Test
    fun take_clearsPending() {
        EpcScanHandoff.offer("epc-raw")
        assertThat(EpcScanHandoff.take()).isEqualTo("epc-raw")
        assertThat(EpcScanHandoff.take()).isNull()
    }

    @Test
    fun offer_overwritesPreviousUnconsumed() {
        EpcScanHandoff.offer("first")
        EpcScanHandoff.offer("second")
        assertThat(EpcScanHandoff.take()).isEqualTo("second")
    }
}
