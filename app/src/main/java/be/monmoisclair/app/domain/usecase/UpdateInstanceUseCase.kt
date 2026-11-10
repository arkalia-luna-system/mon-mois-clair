package be.monmoisclair.app.domain.usecase

import android.content.Context
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseFeeEventEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.receipt.ReceiptLocalFiles
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.PaymentNotePolicy
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.domain.model.ExpenseStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class UpdateInstanceResult {
    data object Success : UpdateInstanceResult()
    data object NotFound : UpdateInstanceResult()
    data class InvalidInput(val message: String) : UpdateInstanceResult()
}

@Singleton
class UpdateInstanceUseCase @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) {

    /**
     * @param intervalMonths
     *   null = ne pas changer la récurrence ;
     *   -1 = passer en « une seule fois » (détache + archive le modèle) ;
     *   1..24 = intervalle (crée le modèle si la ligne était ponctuelle).
     */
    suspend operator fun invoke(
        id: String,
        isExpense: Boolean,
        label: String,
        amountCents: Long,
        dayOfMonth: Int,
        applyToFollowingMonths: Boolean,
        isAutoDebit: Boolean = false,
        paymentNote: String = "",
        receiptRelativePath: String = "",
        intervalMonths: Int? = null,
        causeLabel: String = "",
        /** Null = ne pas changer ; Long = définir si le modèle n’a pas encore de départ. */
        openingRemainingCents: Long? = null,
        /** true = tenter d’écrire [openingRemainingCents] (création / conversion dossier). */
        setOpeningRemaining: Boolean = false,
        /** true = effacer la photo de cette ligne (un chemin vide seul ne l’efface pas). */
        clearReceipt: Boolean = false,
        /** Lot C — IBAN / e-mail / frais (modèle seulement). */
        iban: String = "",
        creditorEmail: String = "",
        /** true = écrire iban/e-mail même vides (fiche dossier). */
        syncCreditorDetails: Boolean = false,
        /** Centimes à **ajouter** aux frais (0 = inchangé). */
        feeAddCents: Long = 0L,
        feeNote: String = "",
        /** Chemin photo à ajouter aux extras (ne remplace pas la principale). */
        extraReceiptAppend: String = "",
        /**
         * Si non null : remplace la liste des courriers en plus (puis [extraReceiptAppend]).
         * Null = ne pas toucher à la liste existante (sauf append).
         */
        extraReceiptPathsReplace: String? = null,
        /**
         * Ligne déjà FAIT : ne pas réécrire son montant ni son jour,
         * ni ceux du modèle (le montant à venir reste celui des Réglages).
         */
        keepDoneAmount: Boolean = false
    ): UpdateInstanceResult {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return UpdateInstanceResult.InvalidInput("Le nom est obligatoire.")
        }
        if (amountCents <= 0L) {
            return UpdateInstanceResult.InvalidInput("Le montant doit être positif.")
        }
        if (dayOfMonth !in 1..31) {
            return UpdateInstanceResult.InvalidInput("Le jour doit être entre 1 et 31.")
        }
        if (intervalMonths != null && intervalMonths != -1 && intervalMonths !in 1..24) {
            return UpdateInstanceResult.InvalidInput("Fréquence invalide.")
        }
        if (setOpeningRemaining && openingRemainingCents != null && openingRemainingCents <= 0L) {
            return UpdateInstanceResult.InvalidInput("Le reste dû doit être positif.")
        }
        val cause = causeLabel.trim()

        if (isExpense) {
            instanceDao.getExpenseById(id) ?: return UpdateInstanceResult.NotFound
        } else {
            instanceDao.getIncomeById(id) ?: return UpdateInstanceResult.NotFound
        }

        return mutatingRepository.mutate(
            sideEffectsWhen = { it is UpdateInstanceResult.Success }
        ) {
            if (isExpense) {
                val localReceiptPathsToDelete = mutableListOf<String>()
                var current = instanceDao.getExpenseById(id)
                    ?: return@mutate UpdateInstanceResult.NotFound
                val ym = YearMonth.parse(current.ledgerId)
                val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
                val auto = isAutoDebit
                val freezeDone = keepDoneAmount && current.status == ExpenseStatus.DONE
                val lineAmount = if (freezeDone) current.amountCents else amountCents
                val lineDay = if (freezeDone) current.dayOfMonth else clamped
                if (clearReceipt && current.templateId == null) {
                    localReceiptPathsToDelete += current.receiptRelativePath
                }
                val receipt = when {
                    clearReceipt -> ""
                    receiptRelativePath.isBlank() -> current.receiptRelativePath
                    else -> receiptRelativePath.trim()
                }

                when {
                    intervalMonths == -1 && current.templateId != null -> {
                        val tid = current.templateId!!
                        templateDao.getExpenseById(tid)?.let { tpl ->
                            if (tpl.active) {
                                templateDao.updateExpense(
                                    tpl.copy(
                                        active = false,
                                        archivedAtEpochMs = AppClock.nowEpochMs()
                                    )
                                )
                            }
                        }
                        val futureTodos = instanceDao.expensesFromTemplateFromLedger(
                            tid,
                            current.ledgerId
                        ).filter { it.id != current.id && it.status == ExpenseStatus.TODO }
                        for (item in futureTodos) {
                            instanceDao.deleteExpense(item)
                            localReceiptPathsToDelete += item.receiptRelativePath
                        }
                        current = current.copy(templateId = null)
                    }
                    intervalMonths != null && intervalMonths > 0 -> {
                        val interval = intervalMonths.coerceIn(1, 24)
                        if (current.templateId == null) {
                            val tid = "tpl-exp-${UUID.randomUUID()}"
                            val now = AppClock.nowEpochMs()
                            val opening = if (setOpeningRemaining) openingRemainingCents else null
                            templateDao.insertExpense(
                                ExpenseTemplateEntity(
                                    id = tid,
                                    label = trimmed,
                                    amountCents = amountCents,
                                    dayOfMonth = dayOfMonth,
                                    active = true,
                                    remindJ3 = true,
                                    remindJd = true,
                                    isAutoDebit = auto,
                                    intervalMonths = interval,
                                    anchorYearMonth = current.ledgerId,
                                    paymentNote = paymentNote.trim(),
                                    receiptRelativePath = if (applyToFollowingMonths) {
                                        receipt
                                    } else {
                                        ""
                                    },
                                    causeLabel = cause,
                                    openingRemainingCents = opening,
                                    createdAtEpochMs = now,
                                    archivedAtEpochMs = null
                                )
                            )
                            current = current.copy(templateId = tid)
                        } else {
                            templateDao.getExpenseById(current.templateId)?.let { tpl ->
                                templateDao.updateExpense(
                                    tpl.copy(intervalMonths = interval)
                                )
                            }
                        }
                    }
                }

                val note = paymentNote.trim()
                instanceDao.updateExpense(
                    current.copy(
                        label = trimmed,
                        amountCents = lineAmount,
                        dayOfMonth = lineDay,
                        isAutoDebit = auto,
                        paymentNote = note,
                        receiptRelativePath = receipt
                    )
                )

                // Lot A : communication (toujours) + photo (si mois suivants).
                // Lot B : causeLabel toujours ; openingRemaining seulement si encore null.
                if (current.templateId != null) {
                    val templateId = current.templateId
                    val existingTpl = templateDao.getExpenseById(templateId)
                    val openingForPropagate = when {
                        existingTpl?.openingRemainingCents != null ->
                            existingTpl.openingRemainingCents
                        setOpeningRemaining -> openingRemainingCents
                        else -> null
                    }
                    existingTpl?.let { tpl ->
                        val opening = openingForPropagate
                        val addCents = feeAddCents.coerceAtLeast(0L)
                        val feesNote = if (addCents > 0L) {
                            feeNote.trim().ifBlank { tpl.addedFeesNote }
                        } else {
                            tpl.addedFeesNote
                        }
                        if (addCents > 0L) {
                            feeEventDao.upsert(
                                ExpenseFeeEventEntity(
                                    id = "fee-${UUID.randomUUID()}",
                                    templateId = templateId,
                                    amountCents = addCents,
                                    note = feeNote.trim(),
                                    createdAtEpochMs = AppClock.nowEpochMs()
                                )
                            )
                        }
                        val fees = tpl.addedFeesCents + addCents
                        val baseExtras = extraReceiptPathsReplace ?: tpl.extraReceiptPaths
                        val extras = if (extraReceiptAppend.isNotBlank()) {
                            ReceiptPathList.append(baseExtras, extraReceiptAppend)
                        } else {
                            baseExtras
                        }
                        if (extraReceiptPathsReplace != null) {
                            val removedExtras = ReceiptPathList.parse(tpl.extraReceiptPaths)
                                .filter { it !in ReceiptPathList.parse(extras) }
                            localReceiptPathsToDelete += removedExtras
                        }
                        val lotC = tpl.copy(
                            iban = if (syncCreditorDetails) iban.trim() else {
                                iban.trim().ifBlank { tpl.iban }
                            },
                            creditorEmail = if (syncCreditorDetails) creditorEmail.trim() else {
                                creditorEmail.trim().ifBlank { tpl.creditorEmail }
                            },
                            addedFeesCents = fees,
                            addedFeesNote = feesNote,
                            extraReceiptPaths = extras
                        )
                        templateDao.updateExpense(
                            if (applyToFollowingMonths) {
                                lotC.copy(
                                    label = trimmed,
                                    amountCents = if (freezeDone) tpl.amountCents else amountCents,
                                    dayOfMonth = if (freezeDone) tpl.dayOfMonth else dayOfMonth,
                                    isAutoDebit = auto,
                                    intervalMonths = when {
                                        intervalMonths != null && intervalMonths > 0 ->
                                            intervalMonths.coerceIn(1, 24)
                                        else -> tpl.intervalMonths
                                    },
                                    paymentNote = note,
                                    receiptRelativePath = receipt,
                                    causeLabel = cause,
                                    openingRemainingCents = opening
                                )
                            } else {
                                lotC.copy(
                                    paymentNote = note,
                                    causeLabel = cause,
                                    openingRemainingCents = opening
                                )
                            }
                        )
                    }
                    if (applyToFollowingMonths) {
                        val others = instanceDao.expensesFromTemplateFromLedger(
                            templateId,
                            current.ledgerId
                        ).filter { it.id != current.id && it.status == ExpenseStatus.TODO }
                        for (item in others) {
                            val itemYm = YearMonth.parse(item.ledgerId)
                            instanceDao.updateExpense(
                                item.copy(
                                    label = trimmed,
                                    amountCents = if (freezeDone) item.amountCents else amountCents,
                                    dayOfMonth = if (freezeDone) {
                                        item.dayOfMonth
                                    } else {
                                        MonthDayClamp.clamp(dayOfMonth, itemYm)
                                    },
                                    isAutoDebit = auto,
                                    paymentNote = note,
                                    receiptRelativePath = receipt
                                )
                            )
                        }
                    } else {
                        // B01 : communication vers mois déjà créés seulement si dossier / prélèvement
                        // (ref stable). Facture ordinaire = ce mois seul (sauf « Aussi les mois suivants »).
                        val carryNote = PaymentNotePolicy.shouldCarryForward(
                            openingRemainingCents = openingForPropagate,
                            isAutoDebit = auto
                        )
                        if (carryNote) {
                            val others = instanceDao.expensesFromTemplateFromLedger(
                                templateId,
                                current.ledgerId
                            ).filter { it.id != current.id && it.status == ExpenseStatus.TODO }
                            for (item in others) {
                                instanceDao.updateExpense(item.copy(paymentNote = note))
                            }
                        }
                    }
                    // Lot B trou audit : baisse montant / frais après FAIT qui avait
                    // effacé le mois suivant → resynchro (même si ligne encore DONE).
                    val refreshed = instanceDao.getExpenseById(id)
                    if (refreshed?.templateId != null) {
                        AgreementFuturesSync.afterAmountOrFeesChange(
                            expense = refreshed,
                            instanceDao = instanceDao,
                            templateDao = templateDao,
                            monthLedgerDao = monthLedgerDao
                        )
                    }
                }
                localReceiptPathsToDelete.forEach { raw ->
                    ReceiptLocalFiles.deleteUnder(appContext.filesDir, raw)
                }
            } else {
                val current = instanceDao.getIncomeById(id)
                    ?: return@mutate UpdateInstanceResult.NotFound
                val ym = YearMonth.parse(current.ledgerId)
                val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
                instanceDao.updateIncome(
                    current.copy(
                        label = trimmed,
                        amountCents = amountCents,
                        dayOfMonth = clamped
                    )
                )
                if (applyToFollowingMonths && current.templateId != null) {
                    val templateId = current.templateId
                    templateDao.getIncomeById(templateId)?.let { tpl ->
                        templateDao.updateIncome(
                            tpl.copy(
                                label = trimmed,
                                amountCents = amountCents,
                                dayOfMonth = dayOfMonth
                            )
                        )
                    }
                    val others = instanceDao.incomesFromTemplateFromLedger(
                        templateId,
                        current.ledgerId
                    ).filter { it.id != current.id }
                    for (item in others) {
                        val itemYm = YearMonth.parse(item.ledgerId)
                        instanceDao.updateIncome(
                            item.copy(
                                label = trimmed,
                                amountCents = amountCents,
                                dayOfMonth = MonthDayClamp.clamp(dayOfMonth, itemYm)
                            )
                        )
                    }
                }
            }
            UpdateInstanceResult.Success
        }
    }
}
