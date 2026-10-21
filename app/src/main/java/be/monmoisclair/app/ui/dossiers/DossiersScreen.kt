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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
        val introShape = MaterialTheme.shapes.medium
        Text(
            text = "Accords et retards — le reste dû est ici, pas sur l’accueil.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, introShape)
                .background(A11yTokens.PaperCard, introShape)
                .padding(14.dp)
        )

        if (state.loading) {
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
                    color = A11yTokens.ButtonFill,
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
        val snap = state.snapshot
        val pending = state.pendingLetters

        if (snap.items.isEmpty() && pending.isEmpty()) {
            val emptyShape = MaterialTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, emptyShape)
                    .background(A11yTokens.PaperCard, emptyShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                color = if (heroCount == 0) A11yTokens.AccentOk else A11yTokens.Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (heroCount == 0) A11yTokens.AccentOk.copy(alpha = 0.35f)
                        else A11yTokens.PaperBorder,
                        heroShape
                    )
                    .background(
                        if (heroCount == 0) A11yTokens.AccentOkSoft else A11yTokens.PaperCard,
                        heroShape
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = heroLabel
                    }
            )

            Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                snap.items.forEach { item ->
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

        Spacer(modifier = Modifier.height(16.dp))
        val pendingIntroShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, pendingIntroShape)
                .background(A11yTokens.PaperCard, pendingIntroShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    .padding(horizontal = 14.dp, vertical = 12.dp)
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
        Spacer(modifier = Modifier.height(12.dp))
        val summaryShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, summaryShape)
                .background(A11yTokens.PaperCard, summaryShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Résumé à montrer",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            Text(
                text = "À envoyer à un créancier ou à un conseiller — pas à tout le monde. " +
                    "Texte simple : revenus, factures, dossiers, mis de côté.",
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
                        color = A11yTokens.ButtonFill,
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
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DossierRow(
    item: DossierListItem,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val progress = if (item.totalDueCents > 0L) {
        (item.paidCents.toFloat() / item.totalDueCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val a11y = buildString {
        append(item.who)
        if (item.what.isNotBlank()) append(", ${item.what}")
        if (item.isSettled) append(", soldé")
        else append(", reste ${MoneyFormat.formatCents(item.remainingCents)}")
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
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                color = A11yTokens.AccentOk,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentOk.copy(alpha = 0.35f), soldShape)
                    .background(A11yTokens.AccentOkSoft, soldShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.small)
                    .semantics {
                        contentDescription =
                            "Déjà versé ${MoneyFormat.formatCents(item.paidCents)} " +
                                "sur ${MoneyFormat.formatCents(item.totalDueCents)}"
                    },
                color = A11yTokens.ButtonFill,
                trackColor = A11yTokens.PaperWarm
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reste ${MoneyFormat.formatCents(item.remainingCents)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
                if (item.monthCents > 0L) {
                    Text(
                        text = MoneyFormat.formatCents(item.monthCents) + " / mois",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingLetterRow(
    letter: PendingLetterEntity,
    onClick: () -> Unit
) {
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
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
