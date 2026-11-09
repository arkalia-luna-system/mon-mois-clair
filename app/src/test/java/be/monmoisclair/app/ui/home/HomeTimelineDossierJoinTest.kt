package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.usecase.DossierListItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeTimelineDossierJoinTest {

    private fun dossier(
        templateId: String,
        openInstanceId: String?,
        remainingPayments: Int?
    ) = DossierListItem(
        templateId = templateId,
        who = "Créancier",
        what = "Accord",
        monthCents = 10_000L,
        remainingCents = 40_000L,
        totalDueCents = 50_000L,
        paidCents = 10_000L,
        isSettled = false,
        openInstanceId = openInstanceId,
        remainingPayments = remainingPayments
    )

    @Test
    fun joinsByTemplateId_whenInstanceIsOtherMonth() {
        val dossiers = listOf(
            dossier(
                templateId = "tpl-a",
                openInstanceId = "exp:2026-11:tpl-a",
                remainingPayments = 5
            )
        )
        val n = HomeTimelineDossierJoin.remainingPaymentsFor(
            rowTemplateId = "tpl-a",
            rowInstanceId = "exp:2026-10:tpl-a",
            dossiers = dossiers
        )
        assertThat(n).isEqualTo(5)
    }

    @Test
    fun fallsBackToOpenInstanceId() {
        val dossiers = listOf(
            dossier(
                templateId = "tpl-b",
                openInstanceId = "exp-open",
                remainingPayments = 2
            )
        )
        val n = HomeTimelineDossierJoin.remainingPaymentsFor(
            rowTemplateId = null,
            rowInstanceId = "exp-open",
            dossiers = dossiers
        )
        assertThat(n).isEqualTo(2)
    }

    @Test
    fun returnsNullWhenNoMatch() {
        val n = HomeTimelineDossierJoin.remainingPaymentsFor(
            rowTemplateId = "other",
            rowInstanceId = "missing",
            dossiers = listOf(
                dossier("tpl-a", "exp-open", 3)
            )
        )
        assertThat(n).isNull()
    }
}
