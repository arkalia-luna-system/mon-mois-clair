package be.monmoisclair.app.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.usecase.UpcomingLine
import be.monmoisclair.app.domain.usecase.UpcomingMonth
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun UpcomingScreen(
    onBack: () -> Unit,
    viewModel: UpcomingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PinnedBackScaffold(onBack = onBack) {
        Text(
            text = "À venir",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        val tipShape = MaterialTheme.shapes.medium
        Text(
            text = "Aperçu des trois prochains mois (lecture seule). " +
                "Rien n’est coché FAIT ici — le mois en cours reste sur l’accueil.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, tipShape)
                .background(A11yTokens.PaperCard, tipShape)
                .padding(tipPad)
        )
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
                text = "Rien de prévu sur les trois prochains mois pour l’instant.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            return@PinnedBackScaffold
        }
        state.months.forEach { month ->
            UpcomingMonthBlock(month = month, tipPad = tipPad)
        }
    }
}

@Composable
private fun UpcomingMonthBlock(month: UpcomingMonth, tipPad: androidx.compose.ui.unit.Dp) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, A11yTokens.PaperBorder, shape)
            .background(A11yTokens.PaperCard, shape)
            .padding(tipPad),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = month.monthLabel,
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink
        )
        month.lines.forEach { line ->
            UpcomingLineRow(line)
        }
    }
}

@Composable
private fun UpcomingLineRow(line: UpcomingLine) {
    val signed = MoneyFormat.formatSignedCents(line.amountCents, isExpense = !line.isIncome)
    val hint = buildString {
        append(if (line.isIncome) "Revenu" else "Dépense")
        if (line.isAgreement) append(" · accord")
        if (line.cadenceHint.isNotBlank()) append(" · ${line.cadenceHint}")
        append(" · le ${line.dayOfMonth}")
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = line.label,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.Ink,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = signed,
                style = MaterialTheme.typography.bodyLarge,
                color = if (line.isIncome) A11yTokens.AccentOkInk else A11yTokens.Ink
            )
        }
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )
    }
}
