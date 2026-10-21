package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import java.time.YearMonth

/**
 * Hub hors ledger — un seul outil (Réglages), puis calendrier des mois.
 * Alertes / aide / restore : dans Réglages (pas de doublon).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeMenuSheet(
    selectedYearMonth: YearMonth,
    isPastMonth: Boolean,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onGoToCurrentMonth: () -> Unit,
    onOpenSettings: () -> Unit,
    /** 0 = pas de bouton (zéro dossier = zéro pixel), sauf courriers sans plan. */
    dossierCount: Int = 0,
    pendingLetterCount: Int = 0,
    onOpenDossiers: () -> Unit = {}
) {
    val showDossiers = dossierCount > 0 || pendingLetterCount > 0
    val menuDossierLabel = when {
        dossierCount > 0 && pendingLetterCount > 0 ->
            "Mes dossiers ($dossierCount) · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
        dossierCount > 0 -> "Mes dossiers ($dossierCount)"
        else ->
            "Mes dossiers · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val current = AppClock.currentYearMonth()
    val months = HomeMonthChoices.recentMonths(current)
    val fit = rememberDisplayFit()
    val menuGap = if (fit.compactHomeChrome) 12.dp else 16.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = A11yTokens.PaperCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = A11yTokens.screenPadding)
                .padding(bottom = if (fit.compactHomeChrome) 20.dp else 28.dp)
        ) {
            AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 6.dp else 8.dp))
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink
            )
            val introShape = MaterialTheme.shapes.medium
            Text(
                text = if (showDossiers) {
                    "Réglages, dossiers, ou un autre mois."
                } else {
                    "Réglages, ou un autre mois."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, introShape)
                    .background(A11yTokens.PaperCard, introShape)
                    .padding(
                        horizontal = 12.dp,
                        vertical = if (fit.compactHomeChrome) 8.dp else 10.dp
                    )
            )
            Spacer(modifier = Modifier.height(menuGap))

            SecondaryA11yButton(
                text = "Réglages",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDossiers) {
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(
                    text = menuDossierLabel,
                    onClick = {
                        onDismiss()
                        onOpenDossiers()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 14.dp else 20.dp))
            val monthsShape = MaterialTheme.shapes.medium
            Text(
                text = "Mois",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, monthsShape)
                    .background(A11yTokens.PaperCard, monthsShape)
                    .padding(
                        horizontal = 12.dp,
                        vertical = if (fit.compactHomeChrome) 8.dp else 10.dp
                    )
            )
            val byYear = months.groupBy { it.year }
            HomeMonthChoices.yearsInOrder(months).forEach { year ->
                val yearMonths = byYear[year] ?: return@forEach
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    yearMonths.forEach { ym ->
                        val selected = ym == selectedYearMonth
                        val chipLabel = DayLabelFormat.monthChipShort(ym)
                        val chipShape = MaterialTheme.shapes.medium
                        Surface(
                            color = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm,
                            shape = chipShape,
                            modifier = Modifier
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .border(
                                    1.dp,
                                    if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                                    chipShape
                                )
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (selected) {
                                        "Mois affiché : $chipLabel"
                                    } else {
                                        "Aller à $chipLabel"
                                    }
                                    this.selected = selected
                                }
                        ) {
                            Text(
                                text = chipLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) A11yTokens.ButtonText else A11yTokens.Ink,
                                modifier = Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Afficher $chipLabel"
                                    ) {
                                        onSelectMonth(ym)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            if (isPastMonth) {
                Spacer(modifier = Modifier.height(10.dp))
                val pastShape = MaterialTheme.shapes.medium
                Text(
                    text = "Vous consultez un mois passé. Les totaux ne bougent plus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, pastShape)
                        .background(A11yTokens.PaperCard, pastShape)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(
                    text = "Revenir au mois en cours",
                    onClick = {
                        onGoToCurrentMonth()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
