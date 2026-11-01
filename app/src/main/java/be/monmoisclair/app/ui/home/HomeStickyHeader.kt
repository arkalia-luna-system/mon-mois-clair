package be.monmoisclair.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.DisplayFit

/**
 * Sticky Accueil : mois · Reste · Courses · épargne · détails « Plus ».
 * Zéro redesign — extrait mécanique de HomeScreen.
 */
@Composable
internal fun HomeStickyHeader(
    state: HomeUiState,
    fit: DisplayFit,
    stickyDetailsOpen: Boolean,
    onStickyDetailsOpenChange: (Boolean) -> Unit,
    onOpenMonthPicker: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenCourses: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenDeadlines: () -> Unit,
    onOpenDossiers: () -> Unit
) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A11yTokens.PaperCream)
                    .padding(horizontal = fit.stickyPaddingHorizontal)
                    .padding(vertical = fit.stickyPaddingVertical),
                // P0-S1 : air entre lignes — le serif respire avant À payer / Courses.
                verticalArrangement = Arrangement.spacedBy(fit.sectionGap)
            ) {
            // ‹ mois › — calendrier via le libellé (plus de 4ᵉ cible). Pas de logo.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthStep(
                    iconRes = R.drawable.ic_chevron_left,
                    description = "Mois précédent",
                    enabled = state.canGoPrev,
                    onClick = onPrevMonth
                )
                Text(
                    text = state.monthLabel,
                    // Quiet sticky : titleMedium muted — le Reste serif commande.
                    style = MaterialTheme.typography.titleMedium,
                    color = A11yTokens.InkMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Choisir un mois"
                        ) { onOpenMonthPicker() }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                )
                MonthStep(
                    iconRes = R.drawable.ic_chevron_right,
                    description = "Mois suivant",
                    enabled = state.canGoNext,
                    onClick = onNextMonth
                )
            }
            val remainingTitle = when {
                state.isFuturePreview && state.summary.remainingCents < 0L ->
                    "Aperçu — déficit après factures"
                state.isFuturePreview -> "Aperçu — reste après factures"
                state.summary.remainingCents < 0L -> "Déficit après factures"
                else -> "Reste après factures"
            }
            val remainingAmount = MoneyFormat.formatCents(state.summary.remainingCents)
            Text(
                text = remainingTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            Text(
                text = remainingAmount,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fit.remainingAmountSp.sp,
                    lineHeight = (fit.remainingAmountSp + 4f).sp,
                    letterSpacing = (-0.02).em
                ),
                color = when {
                    state.summary.remainingCents < 0L -> A11yTokens.DangerInk
                    else -> A11yTokens.Ink
                },
                modifier = Modifier.semantics {
                    contentDescription = "$remainingTitle : $remainingAmount"
                }
            )
            // Air dédié sous le hero avant toute 2ᵉ ligne outil.
            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 6.dp else 8.dp))
            // H01 : 1 chiffre + 1 ligne statut ; Courses = entrée sticky (sheet unique).
            stickyStillToPayLabel(
                stillToPayCents = state.summary.stillToPayCents,
                hasExpenses = state.timeline.any { it is TimelineItem.Expense },
                futurePreview = state.isFuturePreview
            )?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        state.summary.stillToPayCents == 0L -> A11yTokens.AccentOkInk
                        else -> A11yTokens.InkMuted
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { contentDescription = label }
                )
            }
            stickyCoursesLabel(
                diversSpentCents = state.diversSpentCents,
                afterDiversCents = state.afterDiversCents,
                futurePreview = state.isFuturePreview
            )?.let { coursesLabel ->
                StickyLinkRow(
                    label = coursesLabel,
                    // P1-S3 : InkMuted sticky — la brique ne rivalise pas avec le Reste
                    // (Danger reste dans la sheet Courses).
                    labelColor = A11yTokens.InkMuted,
                    compact = fit.compactHomeChrome,
                    onClickLabel = "Modifier les courses",
                    contentDescription = if (state.diversSpentCents > 0L) {
                        "$coursesLabel. Modifier les courses"
                    } else {
                        "Courses. Noter un montant"
                    },
                    onClick = { onOpenCourses() }
                )
            }
            // S26 / hauteur courte : épargne derrière Plus (toujours accessible).
            val savingsLabelSticky = stickySavingsLabel(
                savingsMonthlyCents = state.savingsMonthlyCents,
                futurePreview = state.isFuturePreview
            )
            val savingsBehindPlus =
                fit.compactHomeChrome && savingsLabelSticky != null
            if (!savingsBehindPlus) {
                savingsLabelSticky?.let { savingsLabel ->
                    StickyLinkRow(
                        label = savingsLabel,
                        labelColor = A11yTokens.InkMuted,
                        compact = fit.compactHomeChrome,
                        onClickLabel = "Modifier mettre de côté",
                        contentDescription = if (state.savingsMonthlyCents > 0L) {
                            "$savingsLabel. Modifier"
                        } else {
                            "Mettre de côté. Choisir un montant"
                        },
                        onClick = { onOpenSavings() }
                    )
                }
            }
            // Mode aperçu = contexte, toujours visible (pas derrière « Plus »).
            if (state.isFuturePreview) {
                val previewLabel =
                    "Aperçu — montants d’après la dernière saisie ; pas de FAIT ni d’ajout."
                Text(
                    text = previewLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.AccentWarn,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = previewLabel
                    }
                )
            }
            val afterFaitLabel = stickyWithoutUnpaidLabel(
                remainingCents = state.summary.remainingCents,
                stillToPayCents = state.summary.stillToPayCents,
                futurePreview = state.isFuturePreview
            )
            val needIncomeTip = !state.isFuturePreview &&
                state.summary.remainingCents < 0L &&
                state.timeline.none { it is TimelineItem.Income }
            val hasStickyExtras = afterFaitLabel != null ||
                needIncomeTip ||
                state.nextMonthWarning != null ||
                state.pendingLetterCount > 0 ||
                savingsBehindPlus
            if (hasStickyExtras) {
                // P1-S2 : libellé carnet (« Détails ») — moins chrome UI que « Autres infos ».
                val detailsLabel =
                    if (stickyDetailsOpen) "Masquer" else "Détails"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (stickyDetailsOpen) {
                                "Masquer épargne et détails"
                            } else {
                                "Voir épargne et détails"
                            }
                        ) { onStickyDetailsOpenChange(!stickyDetailsOpen) }
                        .padding(vertical = 4.dp)
                        .semantics { contentDescription = detailsLabel },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // P1-S2 : disclosure légère (bodySmall + chevron 16).
                    Text(
                        text = detailsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = A11yTokens.InkMuted,
                        maxLines = 1
                    )
                    Icon(
                        painter = painterResource(
                            if (stickyDetailsOpen) R.drawable.ic_expand_less
                            else R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        tint = A11yTokens.InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            AnimatedVisibility(visible = stickyDetailsOpen && hasStickyExtras) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(fit.sectionGap)
                ) {
                    if (savingsBehindPlus) {
                        savingsLabelSticky?.let { savingsLabel ->
                            StickyLinkRow(
                                label = savingsLabel,
                                labelColor = A11yTokens.InkMuted,
                                compact = true,
                                onClickLabel = "Modifier mettre de côté",
                                contentDescription = if (state.savingsMonthlyCents > 0L) {
                                    "$savingsLabel. Modifier"
                                } else {
                                    "Mettre de côté. Choisir un montant"
                                },
                                onClick = { onOpenSavings() }
                            )
                        }
                    }
                    afterFaitLabel?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.AccentWarn,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { contentDescription = label }
                        )
                    }
                    if (needIncomeTip) {
                        Text(
                            text = "Ajoutez un revenu (salaire).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.AccentWarn,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "Ajoutez un revenu (salaire)."
                            }
                        )
                    }
                    state.nextMonthWarning?.let { warning ->
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.AccentWarn,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Voir les échéances hors ce mois"
                                ) { onOpenDeadlines() }
                                .semantics {
                                    liveRegion = LiveRegionMode.Polite
                                    contentDescription = warning
                                }
                        )
                    }
                    if (state.pendingLetterCount > 0) {
                        val pendingLabel = if (state.pendingLetterCount == 1) {
                            "1 courrier sans plan · Voir"
                        } else {
                            "${state.pendingLetterCount} courriers sans plan · Voir"
                        }
                        Text(
                            text = pendingLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Ouvrir Dossiers"
                                ) { onOpenDossiers() }
                                .semantics {
                                    liveRegion = LiveRegionMode.Polite
                                    contentDescription = pendingLabel
                                }
                        )
                    }
                }
            }
        }
        }
        // Un seul filet sous le sticky (plus de double séparateur dégradé + divider).
        HorizontalDivider(color = A11yTokens.PaperBorder)
}
