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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import be.monmoisclair.app.domain.usecase.DossierListItem
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.CarnetTitleTokens
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
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

    PinnedBackScaffold(onBack = onBack) {
        CarnetPageTitle("Dossiers")
        Text(
            text = "Reste dû des accords — pas sur l’accueil.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )

        val snap = state.snapshot
        val pending = state.pendingLetters
        // Spinner seulement au tout premier chargement (liste encore vide).
        if (state.loading && snap.items.isEmpty() && pending.isEmpty()) {
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
            // Empty = Tip/Body InkMuted (pas titleLarge Ink type HubRow).
            Text(
                text = "Aucun accord pour le moment.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Sur une facture : « Retard ou accord », puis le reste dû. " +
                    "Sans montant : « Ranger sans plan ».",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            SecondaryA11yButton(
                text = "Ranger un courrier",
                onClick = onAddPending
            )
            return@PinnedBackScaffold
        }

        if (snap.items.isNotEmpty()) {
            // DOS02 : pas de bandeau KPI — titre page + liste (soldé = pastille ligne).
            val openItems = snap.items.filter { !it.isSettled }
            val settledItems = snap.items.filter { it.isSettled }
            if (openItems.isNotEmpty()) {
                DossierSectionHeader("En cours")
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)
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
        if (pending.isEmpty()) {
            Text(
                text = "Aucun pour le moment.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (fit.compactHomeChrome) 6.dp else 8.dp)
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
            text = "Ranger un courrier",
            onClick = onAddPending
        )
        Spacer(modifier = Modifier.height(sectionGap))
        // DOS03 : résumé sans boîte clone Réglages.
        HorizontalDivider(color = A11yTokens.PaperBorder)
        DossierSectionHeader("Résumé à montrer")
        Text(
            text = "Créancier ou conseiller — texte ou PDF, même contenu. " +
                "Le PDF est lisible tout de suite (clair) : ce n’est pas une copie .mmc.",
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
            append(", déjà versé ${MoneyFormat.formatCents(item.paidCents)}")
            append(", reste ${MoneyFormat.formatCents(item.remainingCents)}")
            if (item.pacePhrase != null) append(". ${item.pacePhrase}")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = A11yTokens.Ink.copy(alpha = 0.10f),
                spotColor = A11yTokens.Ink.copy(alpha = 0.08f)
            )
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
            Text(
                text = "Reste ${MoneyFormat.formatCents(item.remainingCents)}",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
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
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = A11yTokens.Ink.copy(alpha = 0.10f),
                spotColor = A11yTokens.Ink.copy(alpha = 0.08f)
            )
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
    }
}
