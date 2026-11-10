package be.monmoisclair.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DossiersOpenFilterTest {

    private fun item(
        overdue: Boolean = false,
        dueMonth: Boolean = false,
        settled: Boolean = false
    ) = DossierListItem(
        templateId = "t",
        who = "X",
        what = "",
        monthCents = 100,
        remainingCents = 1_000,
        totalDueCents = 2_000,
        paidCents = 0,
        isSettled = settled,
        openInstanceId = "i",
        dueThisMonth = dueMonth,
        isOverdueThisMonth = overdue
    )

    @Test
    fun toHandle_includesOverdueOrDueMonth() {
        assertThat(
            DossiersOpenFilterLogic.matches(
                item(overdue = true),
                DossiersOpenFilter.TO_HANDLE
            )
        ).isTrue()
        assertThat(
            DossiersOpenFilterLogic.matches(
                item(dueMonth = true),
                DossiersOpenFilter.TO_HANDLE
            )
        ).isTrue()
        assertThat(
            DossiersOpenFilterLogic.matches(
                item(),
                DossiersOpenFilter.TO_HANDLE
            )
        ).isFalse()
    }

    @Test
    fun thisMonth_onlyDueMonth() {
        assertThat(
            DossiersOpenFilterLogic.matches(
                item(dueMonth = true),
                DossiersOpenFilter.THIS_MONTH
            )
        ).isTrue()
        assertThat(
            DossiersOpenFilterLogic.matches(
                item(overdue = true),
                DossiersOpenFilter.THIS_MONTH
            )
        ).isFalse()
    }
}
