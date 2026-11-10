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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import be.monmoisclair.app.domain.usecase.DossiersOpenFilter
import be.monmoisclair.app.domain.usecase.DossiersOpenFilterLogic
import be.monmoisclair.app.ui.settings.PracticalContextTips
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import be.monmoisclair.app.domain.CarnetListLimits
import be.monmoisclair.app.domain.usecase.DossierListItem
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.ui.components.CarnetTitleTokens
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
import androidx.compose.ui.text.style.TextOverflow
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
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
    val sectionGap = if (fit.compactHomeChrome) 10.dp else 14.dp
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
                .onFailure { viewModel.reportShareIntentFailed() }
        }
    }

    val snap = state.snapshot
    val pending = state.pendingLetters
    val loadingFirst = state.loading && snap.items.isEmpty() && pending.isEmpty()
    // 7.24 : Garder épinglé au-dessus de Retour (liste longue / empty).
    PinnedBackScaffold(
        onBack = onBack,
        pinnedAboveBack = if (!loadingFirst) {
            {
                SecondaryA11yButton(
                    text = "Garder un courrier",
                    onClick = onAddPending
                )
            }
        } else {
            null
        }
    ) {
        CarnetPageTitle("Dossiers")
        CarnetTipCard(text = "Reste dû des accords — pas sur l’accueil.")

        // Spinner seulement au tout premier chargement (liste encore vide).
        if (loadingFirst) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
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
            // 7.23 : empty premium — même carte soft Accueil (pas texte plat).
            val emptyShape = MaterialTheme.shapes.large
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, emptyShape, clip = false)
                    .clip(emptyShape)
                    .background(A11yTokens.PaperWarm)
                    .padding(horizontal = 18.dp, vertical = if (fit.compactHomeChrome) 18.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 8.dp else 10.dp)
            ) {
                Text(
                    text = "Aucun accord pour le moment.",
                    style = MaterialTheme.typography.titleMedium,
                    color = A11yTokens.Ink,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Sur une facture : « Plan de remboursement », puis le reste dû. " +
                        "Sans montant : « Garder le courrier (sans montant) ».",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            return@PinnedBackScaffold
        }

        if (snap.items.isNotEmpty()) {
            var openFilter by rememberSaveable { mutableStateOf(DossiersOpenFilter.ALL) }
            val openItems = snap.items.filter { !it.isSettled }
            val filteredOpen = openItems.filter { DossiersOpenFilterLogic.matches(it, openFilter) }
            val settledItems = snap.items.filter { it.isSettled }
            if (openItems.isNotEmpty()) {
                DossiersFilterRow(
                    selectedFilter = openFilter,
                    onSelect = { openFilter = it }
                )
                CarnetTipCard(text = PracticalContextTips.DOSSIERS_FILTER)
                val kpi = buildString {
                    append(
                        if (openItems.size == 1) "1 accord" else "${openItems.size} accords"
                    )
                    if (snap.totalRemainingCents > 0L) {
                        append(" · reste total ")
                        append(MoneyFormat.formatCents(snap.totalRemainingCents))
                    }
                    if (snap.monthTotalCents > 0L) {
                        append(" · ")
                        append(MoneyFormat.formatCents(snap.monthTotalCents))
                        append(" ce mois")
                    }
                    if (snap.overdueOpenCount > 0) {
                        append(" · ")
                        append(
                            if (snap.overdueOpenCount == 1) {
                                "1 en retard"
                            } else {
                                "${snap.overdueOpenCount} en retard"
                            }
                        )
                    }
                }
                Text(
                    text = kpi,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = kpi }
                )
                DossierSectionHeader("En cours")
                if (filteredOpen.isEmpty() && openFilter != DossiersOpenFilter.ALL) {
                    CarnetTipCard(
                        text = "Rien dans « ${DossiersOpenFilterLogic.chipLabel(openFilter)} ». " +
                            "Essayez Tous."
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                    filteredOpen.forEach { item ->
                        key(item.templateId, item.openInstanceId) {
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
            if (settledItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(sectionGap))
                var settledExpanded by remember { mutableStateOf(false) }
                val settledHeaderLabel =
                    if (settledExpanded) {
                        "Soldés (${settledItems.size}) ▴"
                    } else {
                        "Soldés (${settledItems.size}) ▾"
                    }
                HorizontalDivider(color = A11yTokens.PaperBorder)
                // Même rythme SectionTitle qu’« En cours » (InkMuted), disclosure ▾/▴.
                Text(
                    text = settledHeaderLabel,
                    style = CarnetTitleTokens.sectionTitleStyle(MaterialTheme.typography),
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (settledExpanded) {
                                "Masquer les dossiers soldés"
                            } else {
                                "Afficher les dossiers soldés"
                            }
                        ) { settledExpanded = !settledExpanded }
                        .padding(vertical = 10.dp)
                        .semantics { contentDescription = settledHeaderLabel }
                )
                if (settledExpanded) {
                    val settledSlice = CarnetListLimits.truncate(
                        settledItems,
                        CarnetListLimits.MAX_DOSSIER_SETTLED_VISIBLE
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)
                    ) {
                        settledSlice.visible.forEach { item ->
                            key(item.templateId, item.openInstanceId) {
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
                    CarnetListLimits.hiddenDossiersHint(settledSlice.hiddenCount)?.let { hint ->
                        CarnetTipCard(text = hint)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(sectionGap))
        // SYS01 / PEN01 : titre + tip InkMuted, sans tip-box.
        HorizontalDivider(color = A11yTokens.PaperBorder)
        DossierSectionHeader("Courriers sans plan")
        Text(
            text = "Reçus, sans versement — hors Reste.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (fit.compactHomeChrome) 4.dp else 6.dp)
        )
        if (pending.isNotEmpty()) {
            CarnetTipCard(text = PracticalContextTips.PENDING_LETTERS)
        }
        if (pending.isEmpty()) {
            CarnetTipCard(text = "Aucun pour le moment.")
        } else {
            var confirmDiscardId by remember { mutableStateOf<String?>(null) }
            Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                pending.forEach { letter ->
                    key(letter.id) {
                    PendingLetterRow(
                        letter = letter,
                        onClick = { onConvertPending(letter.id) },
                        onDiscard = { confirmDiscardId = letter.id }
                    )
                    }
                }
            }
            confirmDiscardId?.let { id ->
                val who = pending.firstOrNull { it.id == id }?.who?.trim().orEmpty()
                    .ifBlank { "ce courrier" }
                AlertDialog(
                    onDismissRequest = { confirmDiscardId = null },
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = A11yTokens.PaperCard,
                    title = {
                        Text("Retirer $who ?", color = A11yTokens.Ink)
                    },
                    text = {
                        Text(
                            text = "Le courrier disparaîtra de la liste. " +
                                "Vous pourrez en garder un autre ensuite.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.Ink
                        )
                    },
                    confirmButton = {
                        DangerA11yButton(
                            text = "Retirer",
                            onClick = {
                                confirmDiscardId = null
                                viewModel.discardPendingLetter(id)
                            },
                            fillMaxWidth = false,
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                            onClickLabel = "Retirer définitivement ce courrier"
                        )
                    },
                    dismissButton = {
                        SecondaryA11yButton(
                            text = "Annuler",
                            onClick = { confirmDiscardId = null },
                            fillMaxWidth = false,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(sectionGap))
        // DOS03 : résumé sans boîte clone Réglages.
        HorizontalDivider(color = A11yTokens.PaperBorder)
        DossierSectionHeader("Résumé à montrer")
        Text(
            text = "Créancier ou conseiller — texte ou PDF, même contenu. " +
                "Le PDF est lisible tout de suite (clair) : ce n’est pas une copie de sauvegarde.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        state.summaryMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.summaryMessageError) {
                    CarnetBannerKind.Error
                } else {
                    CarnetBannerKind.Ok
                },
                pad = if (fit.compactHomeChrome) 10.dp else 12.dp
            )
        }
        if (state.summaryBusy) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DossierSectionHeader(title: String) {
    CarnetSectionTitle(title)
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
            if (item.isOverdueThisMonth) append(", en retard ce mois")
            append(", déjà versé ${MoneyFormat.formatCents(item.paidCents)}")
            append(", reste ${MoneyFormat.formatCents(item.remainingCents)}")
            if (item.pacePhrase != null) append(". ${item.pacePhrase}")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(A11yTokens.PaperCard, shape)
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
            Text(
                text = "Soldé",
                style = MaterialTheme.typography.labelLarge,
                color = A11yTokens.AccentOkInk,
                modifier = Modifier
                    .widthIn(min = 48.dp)
                    .background(A11yTokens.AccentOkSoft, StampShape)
                    .border(1.dp, A11yTokens.AccentOkInk.copy(alpha = 0.35f), StampShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        } else {
            // DOS01 : reste commande — pas de barre de progression.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Reste ${MoneyFormat.formatCents(item.remainingCents)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink,
                    modifier = Modifier.weight(1f)
                )
                val monthChip = when {
                    item.isOverdueThisMonth -> "En retard"
                    item.dueThisMonth -> "Ce mois"
                    else -> "Pas ce mois"
                }
                Text(
                    text = monthChip,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.isOverdueThisMonth) {
                        A11yTokens.AccentDanger
                    } else {
                        A11yTokens.InkMuted
                    },
                    modifier = Modifier
                        .background(A11yTokens.PaperWarm, StampShape)
                        .border(
                            1.dp,
                            if (item.isOverdueThisMonth) {
                                A11yTokens.AccentDanger.copy(alpha = 0.35f)
                            } else {
                                A11yTokens.PaperBorder
                            },
                            StampShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "Déjà versé ${MoneyFormat.formatCents(item.paidCents)}" +
                    if (item.cadenceLabel.isNotBlank()) " · ${item.cadenceLabel}" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            if (!item.pacePhrase.isNullOrBlank()) {
                Text(
                    text = item.pacePhrase,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PendingLetterRow(
    letter: PendingLetterEntity,
    onClick: () -> Unit,
    onDiscard: () -> Unit
) {
    val fit = rememberDisplayFit()
    val rowPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    val shape = MaterialTheme.shapes.medium
    val a11y = buildString {
        append(letter.who)
        if (letter.what.isNotBlank()) append(", ${letter.what}")
        append(", courrier sans plan")
        if (letter.receiptRelativePath.isNotBlank()) append(", avec photo")
        append(". Fixer un versement")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(A11yTokens.PaperCard, shape)
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
                "Sans montant · photo"
            } else {
                "Sans montant"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
        // CTA visible (pas seulement carte tappée) — rythme hub ›.
        Text(
            text = "Fixer un versement ›",
            style = MaterialTheme.typography.labelLarge,
            color = A11yTokens.Link,
            modifier = Modifier.padding(top = if (fit.compactHomeChrome) 2.dp else 4.dp)
        )
        QuietLinkA11y(
            text = "Retirer ce courrier",
            onClick = onDiscard,
            onClickLabel = "Retirer ce courrier sans plan"
        )
    }
}
