package be.monmoisclair.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Sticky Accueil (7.08) : mois · Reste + Courses|épargne **à côté** · Détails QuietLink.
 * Outils = colonne droite du hero (1 tap Accueil, pas Menu, pas bandeau sous sticky).
 * Statut factures = tête de liste. Cible pliée Ultra ~110–160 dp.
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
    onOpenDeadlines: () -> Unit,
    onOpenDossiers: () -> Unit,
    onGoToCurrentMonth: () -> Unit = {},
    coursesLabel: String? = null,
    coursesDescription: String? = null,
    onOpenCourses: () -> Unit = {},
    savingsLabel: String? = null,
    savingsDescription: String? = null,
    onOpenSavings: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(A11yTokens.PaperCream)
                .padding(horizontal = fit.stickyPaddingHorizontal)
                .padding(vertical = fit.stickyPaddingVertical),
            verticalArrangement = Arrangement.spacedBy(fit.sectionGap)
        ) {
            // ‹ mois › — calendrier via le libellé. Pas de logo.
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
                        .padding(horizontal = 4.dp, vertical = 4.dp)
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
            val remainingShort = when {
                state.isFuturePreview && state.summary.remainingCents < 0L -> "Aperçu déficit"
                state.isFuturePreview -> "Aperçu reste"
                state.summary.remainingCents < 0L -> "Déficit"
                else -> "Reste"
            }
            val remainingAmount = MoneyFormat.formatCents(state.summary.remainingCents)
            val hasHeroTools = coursesLabel != null || savingsLabel != null
            // Hero : montant + Reste ; Courses|épargne à droite (même rangée, 1 tap).
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "$remainingTitle : $remainingAmount"
                        },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = remainingShort,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (hasHeroTools) {
                    StickyHeroTools(
                        coursesLabel = coursesLabel,
                        coursesDescription = coursesDescription,
                        onOpenCourses = onOpenCourses,
                        savingsLabel = savingsLabel,
                        savingsDescription = savingsDescription,
                        onOpenSavings = onOpenSavings
                    )
                }
            }
            // Mode aperçu = contexte, toujours visible (pas derrière « Détails »).
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
            val showGoCurrent = state.isPastMonth && !state.isFuturePreview
            val hasStickyExtras = afterFaitLabel != null ||
                needIncomeTip ||
                state.nextMonthWarning != null ||
                state.pendingLetterCount > 0 ||
                showGoCurrent
            if (hasStickyExtras) {
                val detailsLabel =
                    if (stickyDetailsOpen) "Masquer" else "Détails"
                // QuietLink disclosure — hit ≥48, pas une bande outil.
                Text(
                    text = detailsLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Link,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (stickyDetailsOpen) {
                                "Masquer les détails"
                            } else {
                                "Voir les détails"
                            }
                        ) { onStickyDetailsOpenChange(!stickyDetailsOpen) }
                        .padding(vertical = 2.dp)
                        .semantics { contentDescription = detailsLabel }
                )
            }
            AnimatedVisibility(visible = stickyDetailsOpen && hasStickyExtras) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(fit.sectionGap)
                ) {
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
                    // Gain 60+ : revenir au mois courant sans ouvrir Menu (mois passé).
                    if (showGoCurrent) {
                        Text(
                            text = "Mois en cours",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.Link,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Revenir au mois en cours"
                                ) { onGoToCurrentMonth() }
                                .semantics {
                                    contentDescription = "Revenir au mois en cours"
                                }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = A11yTokens.PaperBorder)
    }
}
