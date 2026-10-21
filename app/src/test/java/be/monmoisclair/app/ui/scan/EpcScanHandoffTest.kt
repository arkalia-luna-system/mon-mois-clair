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
    fun take_returnsOnceThenNull() {
        EpcScanHandoff.offer("BCD\n2\n1\nBE68")
        assertThat(EpcScanHandoff.take()).isEqualTo("BCD\n2\n1\nBE68")
        assertThat(EpcScanHandoff.take()).isNull()
    }

    @Test
    fun offer_replacesPending() {
        EpcScanHandoff.offer("first")
        EpcScanHandoff.offer("second")
        assertThat(EpcScanHandoff.take()).isEqualTo("second")
    }
}
