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
        assertThat(EntryFormCore.parseMoneyAndDay("10", "0"))
            .isEqualTo(EntryFormCore.MoneyDay.Err(EntryFormCore.AMOUNT_DAY_HINT))
        assertThat(EntryFormCore.parseMoneyAndDay("10", "32"))
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
    fun applyToFollowingOnRecurrenceChange_onceResets() {
        assertThat(
            EntryFormCore.applyToFollowingOnRecurrenceChange(
                RecurrenceChoice.Once,
                hasTemplate = true,
                currentApplyToFollowing = true
            )
        ).isFalse()
        assertThat(
            EntryFormCore.applyToFollowingOnRecurrenceChange(
                RecurrenceChoice.Monthly,
                hasTemplate = true,
                currentApplyToFollowing = false
            )
        ).isTrue()
        assertThat(
            EntryFormCore.applyToFollowingOnRecurrenceChange(
                RecurrenceChoice.Monthly,
                hasTemplate = false,
                currentApplyToFollowing = false
            )
        ).isFalse()
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

    @Test
    fun prepareSharedSave_okAndCreditorGuard() {
        val ok = EntryFormCore.prepareSharedSave(
            amountText = "12,50",
            dayText = "15",
            wantOpening = false,
            openingRemainingText = "",
            recurrence = RecurrenceChoice.Once
        )
        assertThat(ok).isEqualTo(
            EntryFormCore.SharedSavePrep.Ok(
                EntryFormCore.SharedValidated(1250L, 15, null)
            )
        )
        val guarded = EntryFormCore.prepareSharedSave(
            amountText = "10",
            dayText = "1",
            wantOpening = true,
            openingRemainingText = "",
            recurrence = RecurrenceChoice.Monthly,
            creditorGuard = EntryFormCore.CreditorGuard(
                isExpense = true,
                dossierExpanded = true,
                iban = "BE68",
                creditorEmail = ""
            )
        )
        assertThat(guarded).isEqualTo(
            EntryFormCore.SharedSavePrep.Err(EntryFormCore.CREDITOR_WITHOUT_OPENING_HINT)
        )
    }

    @Test
    fun prepareSharedSave_rejectsAgreementOnce() {
        val r = EntryFormCore.prepareSharedSave(
            amountText = "100",
            dayText = "1",
            wantOpening = true,
            openingRemainingText = "50",
            recurrence = RecurrenceChoice.Once
        )
        assertThat(r).isEqualTo(
            EntryFormCore.SharedSavePrep.Err(EntryFormCore.AGREEMENT_ONCE_HINT)
        )
    }

    @Test
    fun recurrenceChoiceFromInterval_mapsKnownValues() {
        assertThat(EntryFormCore.recurrenceChoiceFromInterval(null))
            .isEqualTo(RecurrenceChoice.Once)
        assertThat(EntryFormCore.recurrenceChoiceFromInterval(3))
            .isEqualTo(RecurrenceChoice.Every3Months)
        assertThat(EntryFormCore.recurrenceChoiceFromInterval(12))
            .isEqualTo(RecurrenceChoice.Yearly)
        assertThat(EntryFormCore.recurrenceChoiceFromInterval(1))
            .isEqualTo(RecurrenceChoice.Monthly)
    }

    @Test
    fun resolveLoadedReceiptPaths_templateKeepsInstanceAndExtras() {
        val loaded = EntryFormCore.resolveLoadedReceiptPaths(
            hasTemplate = true,
            instanceReceiptPath = "receipts/a.webp",
            templateExtraPaths = "receipts/b.pdf",
            isPdf = { it.endsWith(".pdf") }
        )
        assertThat(loaded.primaryPath).isEqualTo("receipts/a.webp")
        assertThat(loaded.extrasRaw).isEqualTo("receipts/b.pdf")
    }

    @Test
    fun resolveLoadedReceiptPaths_oneOffSplitsPhotoAndPdf() {
        val raw = "receipts/photo.webp\nreceipts/mail.pdf"
        val loaded = EntryFormCore.resolveLoadedReceiptPaths(
            hasTemplate = false,
            instanceReceiptPath = raw,
            templateExtraPaths = "",
            isPdf = { it.endsWith(".pdf") }
        )
        assertThat(loaded.primaryPath).isEqualTo("receipts/photo.webp")
        assertThat(loaded.extrasRaw).isEqualTo("receipts/mail.pdf")
    }

    @Test
    fun encodeOneOffReceiptPaths_clearedOmitsPrimary() {
        val encoded = EntryFormCore.encodeOneOffReceiptPaths(
            primaryPath = "receipts/old.webp",
            existingExtras = "receipts/mail.pdf",
            cleared = true,
            newPdfRelativePath = "receipts/new.pdf"
        )
        assertThat(encoded).isEqualTo("receipts/mail.pdf\nreceipts/new.pdf")
    }

    @Test
    fun resolveAddFormTarget_clampsFuture() {
        val current = java.time.YearMonth.of(2026, 11)
        val target = EntryFormCore.resolveAddFormTarget("2027-01", current)
        assertThat(target.yearMonth).isEqualTo(current)
        assertThat(target.clampedFromFuture).isTrue()
        assertThat(target.initialError).isNotNull()
        assertThat(target.nextDueMonth).isEqualTo(11)
    }

    @Test
    fun attachRetryError_partialFailures() {
        assertThat(EntryFormCore.attachRetryError(true, true)).isNull()
        assertThat(EntryFormCore.attachRetryError(false, true))
            .isEqualTo(EntryFormCore.ATTACH_RETRY_PHOTO_FAIL)
        assertThat(EntryFormCore.attachRetryError(true, false))
            .isEqualTo(EntryFormCore.ATTACH_RETRY_PDF_FAIL)
        assertThat(EntryFormCore.attachRetryError(false, false))
            .isEqualTo(EntryFormCore.ATTACH_RETRY_BOTH_FAIL)
    }
}
