package be.monmoisclair.app.ui.upcoming

import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.usecase.UpcomingLine
import be.monmoisclair.app.domain.usecase.UpcomingMonth
import be.monmoisclair.app.ui.components.CarnetHubRow
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * À venir : liste des mois → détail lecture seule (entrées / sorties).
 * Pas de FAIT ni d’édition — le mois courant reste sur l’accueil.
 */
@Composable
fun UpcomingScreen(
    onBack: () -> Unit,
    viewModel: UpcomingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val fit = rememberDisplayFit()
    var selectedYmKey by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedMonth = state.months.firstOrNull { it.yearMonth.toString() == selectedYmKey }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val backFromDetailOrScreen: () -> Unit = {
        if (selectedYmKey != null) {
            selectedYmKey = null
        } else {
            onBack()
        }
    }

    PinnedBackScaffold(onBack = backFromDetailOrScreen) {
        if (selectedMonth != null) {
            UpcomingMonthDetail(month = selectedMonth)
            return@PinnedBackScaffold
        }

        CarnetPageTitle("À venir")
        Text(
            text = "Prochains mois (lecture seule). Plus loin : Accueil → calendrier.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 8.dp else 12.dp))

        if (state.loading && state.months.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Chargement de l’aperçu"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.Link,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text("Chargement…", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
            }
            return@PinnedBackScaffold
        }
        if (state.months.isEmpty()) {
            Text(
                text = "Rien de prévu sur les prochains mois.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            return@PinnedBackScaffold
        }
        state.months.forEach { month ->
            val count = month.lines.size
            val suffix = if (count == 1) "1 ligne" else "$count lignes"
            CarnetHubRow(
                label = "${month.monthLabel} · $suffix",
                onClick = { selectedYmKey = month.yearMonth.toString() }
            )
        }
    }
}

@Composable
private fun UpcomingMonthDetail(month: UpcomingMonth) {
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    CarnetPageTitle(month.monthLabel)
    Text(
        text = "Lecture seule — pas de FAIT ici.",
        style = MaterialTheme.typography.bodyLarge,
        color = A11yTokens.InkMuted,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
        month.lines.forEach { line ->
            UpcomingLineCard(line = line, compact = compact)
        }
    }
}

/** Densité type Accueil (carte papier) — sans FAIT ni actions. */
@Composable
private fun UpcomingLineCard(line: UpcomingLine, compact: Boolean) {
    val signed = MoneyFormat.formatSignedCents(line.amountCents, isExpense = !line.isIncome)
    val hint = buildString {
        append(if (line.isIncome) "Revenu" else "Dépense")
        if (line.isAgreement) append(" · accord")
        if (line.cadenceHint.isNotBlank()) append(" · ${line.cadenceHint}")
        append(" · le ${line.dayOfMonth}")
    }
    val shape = MaterialTheme.shapes.medium
    val rowPad = if (compact) 10.dp else 14.dp
    Surface(
        color = A11yTokens.PaperCard,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = A11yTokens.Ink.copy(alpha = 0.10f),
                spotColor = A11yTokens.Ink.copy(alpha = 0.08f)
            )
            .border(1.dp, A11yTokens.PaperBorder, shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight)
                .padding(rowPad),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = line.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = signed,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (line.isIncome) A11yTokens.AccentOkInk else A11yTokens.Ink
                )
            }
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
