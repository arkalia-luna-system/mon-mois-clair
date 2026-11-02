package be.monmoisclair.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.HomeTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
internal fun DayBadge(label: String, compact: Boolean = false) {
    val shape = StampShape
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = HomeTokens.InkMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .widthIn(min = if (compact) 48.dp else 56.dp)
            .background(HomeTokens.Chip, shape)
            .border(1.dp, HomeTokens.CardBorder, shape)
            .padding(
                horizontal = if (compact) 4.dp else 6.dp,
                vertical = if (compact) 4.dp else 4.dp
            )
            .semantics { contentDescription = "Échéance $label" }
    )
}

@Composable
internal fun StatusChip(
    text: String,
    container: Color,
    content: Color,
    outlined: Boolean = false,
    compact: Boolean = false
) {
    val shape = StampShape
    // H04 : air vertical pastille ≥ 4 dp même en compact (pas tampon écrasé).
    val hPad = if (compact) 6.dp else 8.dp
    val vPad = if (compact) 4.dp else 4.dp
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .semantics { contentDescription = "Statut : $text" }
            .border(
                1.dp,
                if (outlined) HomeTokens.InkMuted else HomeTokens.CardBorder,
                shape
            )
            .background(container, shape)
            .padding(horizontal = hPad, vertical = vPad)
    )
}

@Composable
internal fun IncomeRow(
    item: TimelineItem.Income,
    readOnlyPreview: Boolean = false,
    onClick: () -> Unit
) {
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    val rowPad = if (compact) 12.dp else 16.dp
    val shape = HomeTokens.cardRadius
    // 7.10 : carte Accueil (ombre soft + filet léger).
    Surface(
        color = HomeTokens.Card,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(HomeTokens.cardElevation, shape, clip = false)
            .border(1.dp, HomeTokens.CardBorder, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HomeTokens.minTouch)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (readOnlyPreview) "aperçu" else "modifier",
                    onClick = onClick
                )
                .padding(rowPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
        ) {
            DayBadge(item.dayLabel, compact = compact)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = HomeTokens.Ink,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.amountLabel,
                    color = HomeTokens.Ink,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
internal fun ExpenseRow(
    item: TimelineItem.Expense,
    highlighted: Boolean,
    readOnlyPreview: Boolean = false,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onCopyNote: () -> Unit,
    onOpenReceipt: () -> Unit
) {
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    val rowPad = if (compact) 12.dp else 16.dp
    val shape = HomeTokens.cardRadius
    val targetBg = when {
        item.isDone -> HomeTokens.OkSoft
        item.isOverdue -> HomeTokens.DangerFill.copy(alpha = 0.12f)
        else -> HomeTokens.Card
    }
    val bg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(150),
        label = "expenseRowBg"
    )
    val borderMod = when {
        highlighted -> Modifier.border(2.dp, HomeTokens.Link.copy(alpha = 0.55f), shape)
        else -> Modifier.border(1.dp, HomeTokens.CardBorder, shape)
    }
    val lineGap = if (compact) 4.dp else 6.dp
    Surface(
        color = bg,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(HomeTokens.cardElevation, shape, clip = false)
            .then(borderMod)
    ) {
        // H01 : pastille FAIT dans sa bande sous la rangée titre/montant/bouton
        // (plus de collision avec libellé / − montant sur S938B compact).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(rowPad),
            verticalArrangement = Arrangement.spacedBy(lineGap)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
            ) {
                DayBadge(item.dayLabel, compact = compact)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (readOnlyPreview) "aperçu" else "modifier",
                            onClick = onClick
                        ),
                    verticalArrangement = Arrangement.spacedBy(lineGap)
                ) {
                    // Compact + accord : pastille sur la 1ʳᵉ ligne pour ne pas
                    // pousser montant / FAIT (hauteur ~800 dp, plan sortie §3).
                    if (compact && item.isAgreement) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(
                                "Accord",
                                HomeTokens.Chip,
                                HomeTokens.Ink,
                                outlined = true,
                                compact = true
                            )
                        }
                    } else {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = if (compact) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // P0-C1 : premier regard = jour · nom · montant · FAIT.
                    Text(
                        text = item.amountLabel,
                        color = if (item.isDone) HomeTokens.InkMuted else HomeTokens.Ink,
                        style = MaterialTheme.typography.titleLarge
                    )
                    // Cause/cadence : confort seulement (compact = carte allégée).
                    if (!compact) {
                        val meta = buildList {
                            if (item.causeLabel.isNotBlank()) add(item.causeLabel)
                            item.cadenceHint?.let { add(it) }
                        }.joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(
                                text = meta,
                                style = MaterialTheme.typography.bodyMedium,
                                color = HomeTokens.InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (!readOnlyPreview) {
                    val faitModifier = Modifier
                        .widthIn(min = 96.dp)
                        .semantics {
                            contentDescription = if (item.isDone) {
                                "Remettre à payer"
                            } else {
                                "Marquer FAIT"
                            }
                        }
                    if (item.isDone) {
                        SecondaryA11yButton(
                            text = "Remettre",
                            onClick = onToggle,
                            fillMaxWidth = false,
                            modifier = faitModifier
                        )
                    } else {
                        PrimaryA11yButton(
                            text = "FAIT ✓",
                            onClick = onToggle,
                            fillMaxWidth = false,
                            containerColor = HomeTokens.OkFill,
                            modifier = faitModifier
                        )
                    }
                }
            }
            ExpenseStatusLine(
                item = item,
                hideAgreementChip = compact && item.isAgreement
            )
            // H05 : Copier / Photo en 2ᵉ rangée légère — compact = liens seuls (carte allégée).
            val showNote = item.paymentNote.isNotBlank()
            val showPhoto = item.hasReceipt
            if (showNote || showPhoto) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HomeTokens.minTouch)
                ) {
                    if (showNote) {
                        if (!compact) {
                            Text(
                                text = item.paymentNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = HomeTokens.InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        QuietLinkA11y(
                            text = "Copier",
                            onClick = onCopyNote,
                            fillMaxWidth = false,
                            onClickLabel = "Copier la communication"
                        )
                    } else if (showPhoto) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (showPhoto) {
                        QuietLinkA11y(
                            text = if (compact) "Photo" else "Voir la photo",
                            onClick = onOpenReceipt,
                            fillMaxWidth = false,
                            onClickLabel = "Voir la photo de la facture"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExpenseStatusLine(
    item: TimelineItem.Expense,
    hideAgreementChip: Boolean = false
) {
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    // H06 / P0-C1 : max 2 pastilles. Compact = critique seulement (retard / FAIT).
    data class Chip(val text: String, val bg: Color, val fg: Color, val outlined: Boolean = false)
    val chips = buildList {
        when {
            item.isOverdue && !item.isDone ->
                add(Chip("En retard", HomeTokens.DangerFill, A11yTokens.ButtonText))
            item.isDone ->
                add(
                    Chip(
                        item.doneLabel ?: "FAIT ✓",
                        HomeTokens.OkFill,
                        A11yTokens.ButtonText
                    )
                )
        }
        if (!compact) {
            if (item.isAutoDebit) {
                add(
                    Chip(
                        "Prélèvement",
                        HomeTokens.Chip,
                        HomeTokens.InkMuted,
                        outlined = true
                    )
                )
            }
            if (item.isAgreement && !hideAgreementChip && size < 2) {
                add(Chip("Accord", HomeTokens.Chip, HomeTokens.Ink, outlined = true))
            }
        }
    }.take(2)
    val showDebitTip = item.isAutoDebit && !item.isDone
    if (chips.isEmpty() && !showDebitTip) return
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)) {
        if (chips.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
            ) {
                chips.forEach { chip ->
                    StatusChip(
                        chip.text,
                        chip.bg,
                        chip.fg,
                        outlined = chip.outlined,
                        compact = compact
                    )
                }
            }
        }
        // P1-U2 : tip prélèvement visible (pas seulement dans Aide).
        if (showDebitTip) {
            Text(
                text = "Cochez FAIT quand le débit apparaît.",
                style = MaterialTheme.typography.bodyMedium,
                color = HomeTokens.InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
