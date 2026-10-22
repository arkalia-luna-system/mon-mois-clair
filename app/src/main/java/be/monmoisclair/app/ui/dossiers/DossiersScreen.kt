package be.monmoisclair.app.ui.dossiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import be.monmoisclair.app.domain.usecase.DossierListItem
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import kotlinx.coroutines.flow.collectLatest

/**
 * Lot D — Mes dossiers + courriers sans plan.
 * Absent du Menu s’il n’y a ni accord ni courrier.
 */
@Composable
fun DossiersScreen(
    onBack: () -> Unit,
    onOpenInstance: (id: String) -> Unit,
    onOpenTemplate: (id: String) -> Unit,
    onConvertPending: (letterId: String) -> Unit,
    onAddPending: () -> Unit,
    viewModel: DossiersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    val sectionGap = if (fit.compactHomeChrome) 10.dp else 16.dp
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        viewModel.shareIntents.collectLatest { intent ->
            runCatching { context.startActivity(intent) }
        }
    }

    PinnedBackScaffold(onBack = onBack) {
        Text(
            text = "Mes dossiers",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        Text(
            text = "Accords et retards — le reste dû est ici, pas sur l’accueil.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )

        val snap = state.snapshot
        val pending = state.pendingLetters
        // Spinner seulement au tout premier chargement (liste encore vide).
        if (state.loading && snap.items.isEmpty() && pending.isEmpty()) {
            val busyShape = MaterialTheme.shapes.medium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperCard, busyShape)
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Chargement des dossiers"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.Link,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Chargement…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
            return@PinnedBackScaffold
        }

        if (snap.items.isEmpty() && pending.isEmpty()) {
            val emptyShape = MaterialTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, emptyShape)
                    .background(A11yTokens.PaperCard, emptyShape)
                    .padding(if (fit.compactHomeChrome) 12.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(
                    if (fit.compactHomeChrome) 6.dp else 8.dp
                )
            ) {
                Text(
                    text = "Aucun accord pour le moment.",
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
                Text(
                    text = "Sur une facture, ouvrez « C’est un retard ou un accord ? » " +
                        "et indiquez combien il reste aujourd’hui. " +
                        "Sans montant encore : « Ranger sans plan ».",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
            SecondaryA11yButton(
                text = "Ranger un courrier sans plan",
                onClick = onAddPending
            )
            return@PinnedBackScaffold
        }

        if (snap.items.isNotEmpty()) {
            val heroCount = snap.items.count { !it.isSettled }
            val heroLabel = when {
                heroCount == 0 -> "Tout est soldé"
                snap.monthTotalCents > 0L ->
                    "$heroCount dossier${if (heroCount > 1) "s" else ""} · " +
                        MoneyFormat.formatCents(snap.monthTotalCents) + " ce mois"
                else ->
                    "$heroCount dossier${if (heroCount > 1) "s" else ""}"
            }
            val heroShape = MaterialTheme.shapes.medium
            Text(
                text = heroLabel,
                style = MaterialTheme.typography.titleLarge,
                color = if (heroCount == 0) A11yTokens.AccentOkInk else A11yTokens.Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (heroCount == 0) A11yTokens.AccentOkInk.copy(alpha = 0.35f)
                        else A11yTokens.PaperBorder,
                        heroShape
                    )
                    .background(
                        if (heroCount == 0) A11yTokens.AccentOkSoft else A11yTokens.PaperCard,
                        heroShape
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    // liveRegion seul : contentDescription = texte → TalkBack lisait 2×.
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )

            val openItems = snap.items.filter { !it.isSettled }
            val settledItems = snap.items.filter { it.isSettled }
            if (openItems.isNotEmpty()) {
                Text(
                    text = "En cours",
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
                Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                    openItems.forEach { item ->
                        DossierRow(
                            item = item,
                            onClick = {
                                val id = item.openInstanceId
                                if (id != null) onOpenInstance(id)
                                else onOpenTemplate(item.templateId)
                            }
                        )
                    }
                }
            }
            if (settledItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(sectionGap))
                var settledExpanded by remember { mutableStateOf(false) }
                val settledHeaderShape = MaterialTheme.shapes.medium
                val settledHeaderLabel =
                    if (settledExpanded) {
                        "Soldés (${settledItems.size}) — masquer"
                    } else {
                        "Soldés (${settledItems.size}) — afficher"
                    }
                Text(
                    text = settledHeaderLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clip(settledHeaderShape)
                        .border(1.dp, A11yTokens.PaperBorder, settledHeaderShape)
                        .background(A11yTokens.PaperCard, settledHeaderShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (settledExpanded) {
                                "Masquer les dossiers soldés"
                            } else {
                                "Afficher les dossiers soldés"
                            }
                        ) { settledExpanded = !settledExpanded }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .semantics { contentDescription = settledHeaderLabel }
                )
                if (settledExpanded) {
                    Text(
                        text = "Terminés — les mois déjà payés restent dans le calendrier. " +
                            "Touchez une ligne pour revoir la fiche.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        settledItems.forEach { item ->
                            DossierRow(
                                item = item,
                                onClick = {
                                    val id = item.openInstanceId
                                    if (id != null) onOpenInstance(id)
                                    else onOpenTemplate(item.templateId)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(sectionGap))
        val pendingIntroShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, pendingIntroShape)
                .background(A11yTokens.PaperCard, pendingIntroShape)
                .padding(tipPad),
            verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 4.dp else 6.dp)
        ) {
            Text(
                text = "Courriers sans plan",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            Text(
                text = "Reçus, sans versement fixé — rien dans le Reste.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
        if (pending.isEmpty()) {
            val emptyPendingShape = MaterialTheme.shapes.medium
            Text(
                text = "Aucun pour le moment.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, emptyPendingShape)
                    .background(A11yTokens.PaperCard, emptyPendingShape)
                    .padding(
                        horizontal = tipPad,
                        vertical = if (fit.compactHomeChrome) 10.dp else 12.dp
                    )
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                pending.forEach { letter ->
                    PendingLetterRow(
                        letter = letter,
                        onClick = { onConvertPending(letter.id) }
                    )
                }
            }
        }
        SecondaryA11yButton(
            text = "Ranger un courrier sans plan",
            onClick = onAddPending
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 8.dp else 12.dp))
        val summaryShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, summaryShape)
                .background(A11yTokens.PaperCard, summaryShape)
                .padding(tipPad),
            verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 8.dp else 10.dp)
        ) {
            Text(
                text = "Résumé à montrer",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            Text(
                text = "Pour un créancier ou un conseiller — pas à tout le monde. " +
                    "Texte ou PDF (gros caractères), même contenu.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            if (state.summaryBusy) {
                val busyShape = MaterialTheme.shapes.medium
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, busyShape)
                        .background(A11yTokens.PaperCard, busyShape)
                        .padding(12.dp)
                        .semantics {
                            contentDescription = "Préparation du résumé"
                            liveRegion = LiveRegionMode.Polite
                        }
                ) {
                    CircularProgressIndicator(
                        color = A11yTokens.Link,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Préparation…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted
                    )
                }
            }
            SecondaryA11yButton(
                text = if (state.summaryBusy) "Préparation…" else "Préparer un résumé",
                onClick = viewModel::shareSummary,
                enabled = !state.summaryBusy && !state.loading
            )
            SecondaryA11yButton(
                text = if (state.summaryBusy) "Préparation…" else "PDF du résumé",
                onClick = viewModel::shareSummaryPdf,
                enabled = !state.summaryBusy && !state.loading
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DossierRow(
    item: DossierListItem,
    onClick: () -> Unit
) {
    val fit = rememberDisplayFit()
    val rowPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    val shape = MaterialTheme.shapes.medium
    val a11y = buildString {
        append(item.who)
        if (item.what.isNotBlank()) append(", ${item.what}")
        if (item.isSettled) append(", soldé")
        else {
            append(", déjà versé ${MoneyFormat.formatCents(item.paidCents)}")
            append(", reste ${MoneyFormat.formatCents(item.remainingCents)}")
            if (item.pacePhrase != null) append(". ${item.pacePhrase}")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clip(shape)
            .background(A11yTokens.PaperCard)
            .border(1.dp, A11yTokens.PaperBorder, shape)
            .clickable(role = Role.Button, onClickLabel = "Ouvrir le dossier", onClick = onClick)
            .semantics { contentDescription = a11y }
            .padding(rowPad),
        verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 4.dp else 6.dp)
    ) {
        Text(item.who, style = MaterialTheme.typography.titleLarge, color = A11yTokens.Ink)
        if (item.what.isNotBlank()) {
            Text(
                item.what,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
        }
        if (item.isSettled) {
            val soldShape = MaterialTheme.shapes.medium
            Text(
                text = "Soldé",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.AccentOkInk,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentOkInk.copy(alpha = 0.35f), soldShape)
                    .background(A11yTokens.AccentOkSoft, soldShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        } else {
            // DOS01 : typo seule — pas de barre de progression.
            Text(
                text = "Déjà versé ${MoneyFormat.formatCents(item.paidCents)} · " +
                    "reste ${MoneyFormat.formatCents(item.remainingCents)}",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.Ink
            )
            if (item.cadenceLabel.isNotBlank()) {
                Text(
                    text = item.cadenceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
            }
            if (!item.pacePhrase.isNullOrBlank()) {
                Text(
                    text = item.pacePhrase,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PendingLetterRow(
    letter: PendingLetterEntity,
    onClick: () -> Unit
) {
    val fit = rememberDisplayFit()
    val rowPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    val shape = MaterialTheme.shapes.medium
    val a11y = buildString {
        append(letter.who)
        if (letter.what.isNotBlank()) append(", ${letter.what}")
        append(", courrier sans plan")
        if (letter.receiptRelativePath.isNotBlank()) append(", avec photo")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clip(shape)
            .background(A11yTokens.PaperCard)
            .border(1.dp, A11yTokens.PaperBorder, shape)
            .clickable(
                role = Role.Button,
                onClickLabel = "Fixer un versement",
                onClick = onClick
            )
            .semantics { contentDescription = a11y }
            .padding(rowPad),
        verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 2.dp else 4.dp)
    ) {
        Text(letter.who, style = MaterialTheme.typography.titleLarge, color = A11yTokens.Ink)
        if (letter.what.isNotBlank()) {
            Text(
                letter.what,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
        }
        Text(
            text = if (letter.receiptRelativePath.isNotBlank()) {
                "Sans montant · photo · toucher pour fixer un versement"
            } else {
                "Sans montant · toucher pour fixer un versement"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
    }
}
