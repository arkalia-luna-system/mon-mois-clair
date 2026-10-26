package be.monmoisclair.app.ui.entry

import be.monmoisclair.app.ui.format.MoneyFormat
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EntryFormCoreTest {

    @Test
    fun canStartSave_blocksBusyOrFinished() {
        assertThat(EntryFormCore.canStartSave(saving = false, finished = false)).isTrue()
        assertThat(EntryFormCore.canStartSave(saving = true, finished = false)).isFalse()
        assertThat(EntryFormCore.canStartSave(saving = false, finished = true)).isFalse()
    }

    @Test
    fun parseMoneyAndDay_ok() {
        val r = EntryFormCore.parseMoneyAndDay("12,50", "15")
        assertThat(r).isEqualTo(EntryFormCore.MoneyDay.Ok(1250L, 15))
    }

    @Test
    fun parseMoneyAndDay_fractionHint() {
        val r = EntryFormCore.parseMoneyAndDay("12,345", "1")
        assertThat(r).isEqualTo(EntryFormCore.MoneyDay.Err(MoneyFormat.FRACTION_HINT))
    }

    @Test
    fun parseMoneyAndDay_badDayOrAmount() {
        assertThat(EntryFormCore.parseMoneyAndDay("abc", "1"))
            .isEqualTo(EntryFormCore.MoneyDay.Err(EntryFormCore.AMOUNT_DAY_HINT))
        assertThat(EntryFormCore.parseMoneyAndDay("10", "x"))
            .isEqualTo(EntryFormCore.MoneyDay.Err(EntryFormCore.AMOUNT_DAY_HINT))
    }

    @Test
    fun parseMoneyAndDay_lineMax() {
        val tooMuch = (MoneyFormat.MAX_LINE_EUROS + 1).toString()
        val r = EntryFormCore.parseMoneyAndDay(tooMuch, "1")
        assertThat(r).isEqualTo(EntryFormCore.MoneyDay.Err(MoneyFormat.LINE_MAX_HINT))
    }

    @Test
    fun parseOpening_noneWhenNotWantedOrEmpty() {
        assertThat(EntryFormCore.parseOpeningRemaining(false, "100"))
            .isEqualTo(EntryFormCore.Opening.None)
        assertThat(EntryFormCore.parseOpeningRemaining(true, "  "))
            .isEqualTo(EntryFormCore.Opening.None)
    }

    @Test
    fun parseOpening_okAndErrors() {
        assertThat(EntryFormCore.parseOpeningRemaining(true, "200,00"))
            .isEqualTo(EntryFormCore.Opening.Ok(20_000L))
        assertThat(EntryFormCore.parseOpeningRemaining(true, "xx"))
            .isEqualTo(EntryFormCore.Opening.Err(EntryFormCore.OPENING_HINT))
        val huge = (MoneyFormat.MAX_OPENING_EUROS + 1).toString()
        assertThat(EntryFormCore.parseOpeningRemaining(true, huge))
            .isEqualTo(EntryFormCore.Opening.Err(MoneyFormat.OPENING_MAX_HINT))
    }

    @Test
    fun agreementRecurrence_rejectsOnce() {
        assertThat(
            EntryFormCore.agreementRecurrenceError(100L, RecurrenceChoice.Once)
        ).isEqualTo(EntryFormCore.AGREEMENT_ONCE_HINT)
        assertThat(
            EntryFormCore.agreementRecurrenceError(100L, RecurrenceChoice.Monthly)
        ).isNull()
        assertThat(
            EntryFormCore.agreementRecurrenceError(null, RecurrenceChoice.Once)
        ).isNull()
    }

    @Test
    fun creditorBitsWithoutOpening_addGuard() {
        assertThat(
            EntryFormCore.creditorBitsWithoutOpeningError(
                isExpense = true,
                dossierExpanded = true,
                openingCents = null,
                iban = "BE68",
                creditorEmail = ""
            )
        ).isEqualTo(EntryFormCore.CREDITOR_WITHOUT_OPENING_HINT)
        assertThat(
            EntryFormCore.creditorBitsWithoutOpeningError(
                isExpense = true,
                dossierExpanded = true,
                openingCents = 1L,
                iban = "BE68",
                creditorEmail = ""
            )
        ).isNull()
    }

    @Test
    fun parseFeeAdd_editRules() {
        assertThat(EntryFormCore.parseFeeAdd(false, "10", "")).isEqualTo(EntryFormCore.FeeAdd.Ok(0L))
        assertThat(EntryFormCore.parseFeeAdd(true, "", "motif"))
            .isEqualTo(EntryFormCore.FeeAdd.Err(EntryFormCore.FEE_NOTE_WITHOUT_AMOUNT_HINT))
        assertThat(EntryFormCore.parseFeeAdd(true, "0", ""))
            .isEqualTo(EntryFormCore.FeeAdd.Err(EntryFormCore.FEE_AMOUNT_HINT))
        assertThat(EntryFormCore.parseFeeAdd(true, "5,00", "huissier"))
            .isEqualTo(EntryFormCore.FeeAdd.Ok(500L))
    }

    @Test
    fun intervalHelpers() {
        assertThat(EntryFormCore.intervalMonths(RecurrenceChoice.Every3Months)).isEqualTo(3)
        assertThat(
            EntryFormCore.editIntervalMonths(true, RecurrenceChoice.Once, hasTemplate = true)
        ).isEqualTo(-1)
        assertThat(
            EntryFormCore.editIntervalMonths(true, RecurrenceChoice.Once, hasTemplate = false)
        ).isNull()
        assertThat(
            EntryFormCore.editIntervalMonths(false, RecurrenceChoice.Monthly, hasTemplate = true)
        ).isNull()
    }

    @Test
    fun pendingLetterCheck() {
        assertThat(
            EntryFormCore.pendingLetterCheck(true, false, "", "X")
        ).isEqualTo(EntryFormCore.PendingLetterCheck.AbortSilent)
        assertThat(
            EntryFormCore.pendingLetterCheck(true, true, "", "")
        ).isEqualTo(
            EntryFormCore.PendingLetterCheck.Abort(EntryFormCore.PENDING_LETTER_WHO)
        )
        assertThat(
            EntryFormCore.pendingLetterCheck(true, true, "50", "Créancier")
        ).isEqualTo(
            EntryFormCore.PendingLetterCheck.Abort(EntryFormCore.PENDING_LETTER_HAS_OPENING)
        )
        assertThat(
            EntryFormCore.pendingLetterCheck(true, true, "", "Créancier")
        ).isEqualTo(EntryFormCore.PendingLetterCheck.Proceed)
    }
}
