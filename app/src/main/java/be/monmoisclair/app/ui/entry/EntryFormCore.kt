package be.monmoisclair.app.ui.entry

import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.format.MoneyFormat
import java.time.YearMonth

/**
 * Cœur métier partagé Add / Edit : validation save, récurrence, load chemins reçus,
 * garde anti-double-tap. Les ViewModels gardent I/O (DAO, photos) et l’état UI spécifique.
 */
object EntryFormCore {

    sealed class MoneyDay {
        data class Ok(val cents: Long, val day: Int) : MoneyDay()
        data class Err(val message: String) : MoneyDay()
    }

    sealed class Opening {
        /** Pas de reste dû demandé (bloc fermé, déjà accord, ou champ vide). */
        data object None : Opening()
        data class Ok(val cents: Long) : Opening()
        data class Err(val message: String) : Opening()
    }

    sealed class FeeAdd {
        data class Ok(val cents: Long) : FeeAdd()
        data class Err(val message: String) : FeeAdd()
    }

    /** Anti-doublon Enregistrer (saving / déjà sauvé). */
    fun canStartSave(saving: Boolean, finished: Boolean): Boolean =
        !saving && !finished

    fun parseMoneyAndDay(amountText: String, dayText: String): MoneyDay {
        if (MoneyFormat.tooManyFractionDigits(amountText)) {
            return MoneyDay.Err(MoneyFormat.FRACTION_HINT)
        }
        val cents = MoneyFormat.parseToCents(amountText)
        val day = dayText.trim().toIntOrNull()
        if (cents == null || day == null || day !in 1..31) {
            return MoneyDay.Err(AMOUNT_DAY_HINT)
        }
        if (MoneyFormat.isOverLineMax(cents)) {
            return MoneyDay.Err(MoneyFormat.LINE_MAX_HINT)
        }
        return MoneyDay.Ok(cents, day)
    }

    /**
     * @param wantOpening true = conversion dossier (Add : bloc ouvert ; Edit : ouvert et pas encore accord).
     */
    fun parseOpeningRemaining(wantOpening: Boolean, openingRaw: String): Opening {
        if (!wantOpening) return Opening.None
        val raw = openingRaw.trim()
        if (raw.isEmpty()) return Opening.None
        val cents = MoneyFormat.parseToCents(raw)
            ?: return Opening.Err(OPENING_HINT)
        if (MoneyFormat.isOverOpeningMax(cents)) {
            return Opening.Err(MoneyFormat.OPENING_MAX_HINT)
        }
        return Opening.Ok(cents)
    }

    /** Un accord (reste dû) exige une facture qui revient. */
    fun agreementRecurrenceError(
        openingCents: Long?,
        recurrence: RecurrenceChoice
    ): String? {
        if (openingCents == null) return null
        if (recurrence != RecurrenceChoice.Once) return null
        return AGREEMENT_ONCE_HINT
    }

    /**
     * Add seulement : IBAN / e-mail renseignés sans reste dû → refus
     * (évite de croire qu’un dossier a été créé).
     */
    fun creditorBitsWithoutOpeningError(
        isExpense: Boolean,
        dossierExpanded: Boolean,
        openingCents: Long?,
        iban: String,
        creditorEmail: String
    ): String? {
        if (!isExpense || !dossierExpanded || openingCents != null) return null
        val hasBits = iban.isNotBlank() || creditorEmail.isNotBlank()
        if (!hasBits) return null
        return CREDITOR_WITHOUT_OPENING_HINT
    }

    /** Edit : frais additionnels sur un accord existant. */
    fun parseFeeAdd(
        isAgreement: Boolean,
        feeAddText: String,
        feeNoteText: String
    ): FeeAdd {
        if (!isAgreement) return FeeAdd.Ok(0L)
        val raw = feeAddText.trim()
        if (raw.isEmpty()) {
            if (feeNoteText.isNotBlank()) {
                return FeeAdd.Err(FEE_NOTE_WITHOUT_AMOUNT_HINT)
            }
            return FeeAdd.Ok(0L)
        }
        val cents = MoneyFormat.parseToCents(raw)
        if (cents == null || cents <= 0L) {
            return FeeAdd.Err(FEE_AMOUNT_HINT)
        }
        if (MoneyFormat.isOverLineMax(cents)) {
            return FeeAdd.Err(MoneyFormat.LINE_MAX_HINT)
        }
        return FeeAdd.Ok(cents)
    }

    fun intervalMonths(recurrence: RecurrenceChoice): Int = when (recurrence) {
        RecurrenceChoice.Once, RecurrenceChoice.Monthly -> 1
        RecurrenceChoice.Every3Months -> 3
        RecurrenceChoice.Yearly -> 12
    }

    /**
     * Edit : « Appliquer aux mois suivants » n’a pas de sens sur « une seule fois » ;
     * repasse à false pour éviter delete / état dirty trompeurs avant enregistrement.
     */
    fun applyToFollowingOnRecurrenceChange(
        newRecurrence: RecurrenceChoice,
        hasTemplate: Boolean,
        currentApplyToFollowing: Boolean
    ): Boolean = when {
        newRecurrence == RecurrenceChoice.Once -> false
        hasTemplate -> true
        else -> currentApplyToFollowing
    }

    /** Edit load : intervalle template → choix UI (null = une fois). */
    fun recurrenceChoiceFromInterval(intervalMonths: Int?): RecurrenceChoice = when (intervalMonths) {
        null -> RecurrenceChoice.Once
        3 -> RecurrenceChoice.Every3Months
        12 -> RecurrenceChoice.Yearly
        else -> RecurrenceChoice.Monthly
    }

    /**
     * Edit : null = inchangé ; -1 = repasser en une fois ; 1/3/12 = intervalle.
     */
    fun editIntervalMonths(
        isExpense: Boolean,
        recurrence: RecurrenceChoice,
        hasTemplate: Boolean
    ): Int? {
        if (!isExpense) return null
        return when (recurrence) {
            RecurrenceChoice.Once -> if (hasTemplate) -1 else null
            RecurrenceChoice.Monthly -> 1
            RecurrenceChoice.Every3Months -> 3
            RecurrenceChoice.Yearly -> 12
        }
    }

    /**
     * Chemins reçus au load Edit :
     * modèle → photo instance + extras template ; une fois → photo (+ PDF) dans le même champ.
     */
    data class LoadedReceiptPaths(
        val primaryPath: String,
        val extrasRaw: String
    )

    fun resolveLoadedReceiptPaths(
        hasTemplate: Boolean,
        instanceReceiptPath: String,
        templateExtraPaths: String,
        isPdf: (String) -> Boolean
    ): LoadedReceiptPaths {
        if (hasTemplate) {
            return LoadedReceiptPaths(
                primaryPath = instanceReceiptPath,
                extrasRaw = templateExtraPaths
            )
        }
        return LoadedReceiptPaths(
            primaryPath = ReceiptPathList.primaryPhotoOrFirst(instanceReceiptPath, isPdf),
            extrasRaw = ReceiptPathList.encode(
                ReceiptPathList.extrasBesidesPrimary(instanceReceiptPath, isPdf)
            )
        )
    }

    /**
     * Facture une fois : recomposer photo (+ extras déjà là) (+ PDF neuf optionnel).
     * @param cleared true = l’utilisateur a enlevé la photo (ne pas réinjecter primary).
     */
    fun encodeOneOffReceiptPaths(
        primaryPath: String,
        existingExtras: String,
        cleared: Boolean,
        newPdfRelativePath: String? = null
    ): String = ReceiptPathList.encode(
        buildList {
            if (primaryPath.isNotBlank() && !cleared) add(primaryPath)
            addAll(ReceiptPathList.parse(existingExtras))
            val pdf = newPdfRelativePath?.trim().orEmpty()
            if (pdf.isNotEmpty()) add(pdf)
        }
    )

    /** Add — clamp mois cible + titre / message (équivalent « load » initial). */
    data class AddFormTarget(
        val yearMonth: YearMonth,
        val clampedFromFuture: Boolean,
        val formTitle: String,
        val initialError: String?,
        val nextDueMonth: Int
    )

    fun resolveAddFormTarget(
        rawYm: String?,
        current: YearMonth = AppClock.currentYearMonth()
    ): AddFormTarget {
        val parsed = runCatching { YearMonth.parse(rawYm) }.getOrNull()
        val (ym, clamped) = when {
            parsed == null -> current to false
            parsed.isAfter(current) -> current to true
            else -> parsed to false
        }
        val title = if (ym == current) {
            "Nouvelle facture ou revenu"
        } else {
            "Nouvelle facture ou revenu · ${DayLabelFormat.monthTitle(ym)}"
        }
        return AddFormTarget(
            yearMonth = ym,
            clampedFromFuture = clamped,
            formTitle = title,
            initialError = if (clamped) {
                "On ajoute sur le mois en cours — pas sur un mois à venir."
            } else {
                null
            },
            nextDueMonth = ym.monthValue
        )
    }

    const val ATTACH_RETRY_NEED =
        "Reprenez une photo ou un PDF, puis Enregistrer."
    const val ATTACH_RETRY_BOTH_FAIL =
        "Photo et PDF n’ont pas pu être enregistrés."
    const val ATTACH_RETRY_PHOTO_FAIL =
        "La photo n’a pas pu être enregistrée."
    const val ATTACH_RETRY_PDF_FAIL =
        "Le PDF n’a pas pu être enregistré."

    fun attachRetryError(photoOk: Boolean, pdfOk: Boolean): String? = when {
        photoOk && pdfOk -> null
        !photoOk && !pdfOk -> ATTACH_RETRY_BOTH_FAIL
        !photoOk -> ATTACH_RETRY_PHOTO_FAIL
        else -> ATTACH_RETRY_PDF_FAIL
    }

    sealed class PendingLetterCheck {
        data object Proceed : PendingLetterCheck()
        /** Bloc fermé / pas dépense — pas de message d’erreur. */
        data object AbortSilent : PendingLetterCheck()
        data class Abort(val message: String) : PendingLetterCheck()
    }

    /** Add — ranger courrier sans plan (pas de reste dû). */
    fun pendingLetterCheck(
        isExpense: Boolean,
        dossierExpanded: Boolean,
        openingRemainingText: String,
        whoLabel: String
    ): PendingLetterCheck {
        if (!isExpense || !dossierExpanded) return PendingLetterCheck.AbortSilent
        if (openingRemainingText.trim().isNotEmpty()) {
            return PendingLetterCheck.Abort(PENDING_LETTER_HAS_OPENING)
        }
        if (whoLabel.trim().isEmpty()) {
            return PendingLetterCheck.Abort(PENDING_LETTER_WHO)
        }
        return PendingLetterCheck.Proceed
    }

    /**
     * Prévalidation commune Add/Edit avant I/O :
     * montant+jour → reste dû → récurrence accord → (optionnel) garde créancier Add.
     */
    data class SharedValidated(
        val cents: Long,
        val day: Int,
        val openingCents: Long?
    )

    data class CreditorGuard(
        val isExpense: Boolean,
        val dossierExpanded: Boolean,
        val iban: String,
        val creditorEmail: String
    )

    sealed class SharedSavePrep {
        data class Ok(val validated: SharedValidated) : SharedSavePrep()
        data class Err(val message: String) : SharedSavePrep()
    }

    fun prepareSharedSave(
        amountText: String,
        dayText: String,
        wantOpening: Boolean,
        openingRemainingText: String,
        recurrence: RecurrenceChoice,
        creditorGuard: CreditorGuard? = null
    ): SharedSavePrep {
        val moneyDay = parseMoneyAndDay(amountText, dayText)
        if (moneyDay is MoneyDay.Err) return SharedSavePrep.Err(moneyDay.message)
        val (cents, day) = moneyDay as MoneyDay.Ok
        val openingParsed = parseOpeningRemaining(wantOpening, openingRemainingText)
        if (openingParsed is Opening.Err) return SharedSavePrep.Err(openingParsed.message)
        val openingCents = (openingParsed as? Opening.Ok)?.cents
        agreementRecurrenceError(openingCents, recurrence)?.let {
            return SharedSavePrep.Err(it)
        }
        if (creditorGuard != null) {
            creditorBitsWithoutOpeningError(
                isExpense = creditorGuard.isExpense,
                dossierExpanded = creditorGuard.dossierExpanded,
                openingCents = openingCents,
                iban = creditorGuard.iban,
                creditorEmail = creditorGuard.creditorEmail
            )?.let { return SharedSavePrep.Err(it) }
        }
        return SharedSavePrep.Ok(SharedValidated(cents, day, openingCents))
    }

    const val AMOUNT_DAY_HINT = "Vérifiez le montant et le jour (1 à 31)."
    const val OPENING_HINT = "Vérifiez le reste dû (montant)."
    const val AGREEMENT_ONCE_HINT =
        "Un accord doit être une facture qui revient (mensuel, 3 mois ou annuel)."
    const val CREDITOR_WITHOUT_OPENING_HINT =
        "Indiquez le reste dû, ou « Garder le courrier (sans montant) », ou videz l’IBAN et l’e-mail."
    const val FEE_AMOUNT_HINT = "Vérifiez le montant des frais."
    const val FEE_NOTE_WITHOUT_AMOUNT_HINT =
        "Indiquez le montant des frais avec le motif."
    const val PENDING_LETTER_HAS_OPENING =
        "Videz le reste dû pour garder le courrier sans montant, ou Enregistrer un accord."
    const val PENDING_LETTER_WHO = "Indiquez à qui (le nom en haut)."
}
