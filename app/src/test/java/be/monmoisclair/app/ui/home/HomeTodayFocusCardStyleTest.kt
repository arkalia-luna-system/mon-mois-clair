package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeTodayFocusCardStyleTest {

    @Test
    fun overdue_usesThickerDangerBorder() {
        assertThat(HomeTodayFocusCardStyle.overdueBorderWidthDp(true)).isEqualTo(1.5f)
        assertThat(HomeTodayFocusCardStyle.overdueBorderWidthDp(false)).isEqualTo(0.5f)
    }

    @Test
    fun overdueBorder_onlyWhenOverdue() {
        assertThat(HomeTodayFocusCardStyle.usesOverdueBorder(true)).isTrue()
        assertThat(HomeTodayFocusCardStyle.usesOverdueBorder(false)).isFalse()
    }

    @Test
    fun actionLabel_matchesContext() {
        assertThat(HomeTodayFocusCardStyle.actionLabel(true)).isEqualTo("Voir les retards")
        assertThat(HomeTodayFocusCardStyle.actionLabel(false)).isEqualTo("Voir l’échéance")
    }
}
