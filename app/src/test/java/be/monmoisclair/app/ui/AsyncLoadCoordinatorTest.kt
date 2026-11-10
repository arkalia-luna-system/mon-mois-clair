package be.monmoisclair.app.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AsyncLoadCoordinatorTest {

    @Test
    fun onlyLatestLoadIsCurrent() {
        val coordinator = AsyncLoadCoordinator()
        val first = coordinator.startLoad()
        assertThat(coordinator.isStale(first)).isFalse()
        coordinator.startLoad()
        assertThat(coordinator.isStale(first)).isTrue()
        val latest = coordinator.startLoad()
        assertThat(coordinator.isStale(latest)).isFalse()
    }
}
