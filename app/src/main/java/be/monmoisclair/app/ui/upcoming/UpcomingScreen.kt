package be.monmoisclair.app.ui.upcoming

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
import be.monmoisclair.app.R
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
        val introShape = MaterialTheme.shapes.large
        Text(
            text = "Aperçu des mois suivants (sans FAIT). Pour un mois précis, " +
                "ouvrez le calendrier sur l’accueil.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clip(introShape)
                .background(A11yTokens.PaperWarm)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 10.dp else 14.dp))

        if (state.loading && state.months.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(introShape)
                    .background(A11yTokens.PaperCard)
                    .padding(16.dp)
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
                style = MaterialTheme.typography.titleMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(introShape)
                    .background(A11yTokens.PaperCard)
                    .padding(horizontal = 18.dp, vertical = 22.dp)
            )
            return@PinnedBackScaffold
        }
        state.months.forEach { month ->
            val expenses = month.lines.count { !it.isIncome }
            val incomes = month.lines.count { it.isIncome }
            val suffix = when {
                expenses > 0 && incomes == 0 ->
                    if (expenses == 1) "1 facture" else "$expenses factures"
                incomes > 0 && expenses == 0 ->
                    if (incomes == 1) "1 revenu" else "$incomes revenus"
                else -> {
                    val n = month.lines.size
                    if (n == 1) "1 ligne" else "$n lignes"
                }
            }
            CarnetHubRow(
                label = "${month.monthLabel} · $suffix",
                onClick = { selectedYmKey = month.yearMonth.toString() },
                leadingIconRes = R.drawable.ic_kit_calendar,
                leadingColoredIcon = true
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
        style = MaterialTheme.typography.bodyMedium,
        color = A11yTokens.InkMuted,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(A11yTokens.PaperWarm)
            .padding(horizontal = 14.dp, vertical = 10.dp)
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
    val rowPad = if (compact) 12.dp else 16.dp
    // 7.13 : carte soft ombre (plus filet carnet seul).
    Surface(
        color = A11yTokens.PaperCard,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
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
