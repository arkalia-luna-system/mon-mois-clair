package be.monmoisclair.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.ui.components.CarnetHubRow
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance
import java.time.YearMonth

/**
 * Hub hors ledger — outils puis mois (récents + autres, pas un mur de pastilles).
 * Aide / restore : dans Réglages. Alertes = signal en tête si bloquées (pas de remplacement Menu).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeMenuSheet(
    selectedYearMonth: YearMonth,
    isPastMonth: Boolean,
    isFuturePreview: Boolean = false,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onGoToCurrentMonth: () -> Unit,
    onOpenSettings: () -> Unit,
    /** 0 = pas de bouton (zéro dossier = zéro pixel), sauf courriers sans plan. */
    dossierCount: Int = 0,
    pendingLetterCount: Int = 0,
    onOpenDossiers: () -> Unit = {},
    onOpenUpcoming: () -> Unit = {},
    remindersBlocked: Boolean = false,
    onOpenHealth: () -> Unit = {}
) {
    val showDossiers = dossierCount > 0 || pendingLetterCount > 0
    val menuDossierLabel = when {
        dossierCount > 0 && pendingLetterCount > 0 ->
            "Dossiers ($dossierCount) · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
        dossierCount > 0 -> "Dossiers ($dossierCount)"
        else ->
            "Dossiers · $pendingLetterCount courrier" +
                if (pendingLetterCount > 1) "s" else ""
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val current = AppClock.currentYearMonth()
    val recent = remember(current) { HomeMonthChoices.recentStrip(current) }
    val otherByYear = remember(current, recent) {
        HomeMonthChoices.otherMonthsByYear(current, recent)
    }
    val fit = rememberDisplayFit()
    val menuGap = if (fit.compactHomeChrome) 10.dp else 14.dp
    val sheetBottom = rememberSheetBottomClearance()
    val selectedInOther = otherByYear.any { (_, months) -> selectedYearMonth in months }
    var expandedYear by remember(selectedYearMonth, otherByYear) {
        mutableStateOf(
            if (selectedInOther) {
                otherByYear.firstOrNull { (_, months) -> selectedYearMonth in months }?.first
            } else {
                null
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = A11yTokens.PaperCream,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = sheetBottom)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = A11yTokens.screenPadding)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink
            )
            Spacer(modifier = Modifier.height(menuGap))

            if (remindersBlocked) {
                CarnetHubRow(
                    label = "Alertes",
                    onClick = {
                        onDismiss()
                        onOpenHealth()
                    },
                    labelColor = A11yTokens.AccentWarn,
                    showDivider = false
                )
            }

            CarnetHubRow(
                label = "Réglages",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                },
                showDivider = !remindersBlocked
            )
            if (showDossiers) {
                CarnetHubRow(
                    label = menuDossierLabel,
                    onClick = {
                        onDismiss()
                        onOpenDossiers()
                    }
                )
            }
            CarnetHubRow(
                label = "À venir",
                onClick = {
                    onDismiss()
                    onOpenUpcoming()
                }
            )

            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 16.dp else 22.dp))
            Text(
                text = "Mois",
                style = MaterialTheme.typography.titleLarge,
                color = A11yTokens.Ink
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mois récents",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            recent.forEach { ym ->
                val label = DayLabelFormat.monthTitle(ym)
                CarnetHubRow(
                    label = label,
                    onClick = {
                        onSelectMonth(ym)
                        onDismiss()
                    },
                    labelColor = if (ym == selectedYearMonth) A11yTokens.Link else A11yTokens.Ink
                )
            }

            if (otherByYear.isNotEmpty()) {
                Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 14.dp else 18.dp))
                Text(
                    text = "Autres mois",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
                otherByYear.forEach { (year, yearMonths) ->
                    val expanded = expandedYear == year
                    CarnetHubRow(
                        label = if (expanded) "$year ▾" else year.toString(),
                        onClick = {
                            expandedYear = if (expanded) null else year
                        },
                        chevronColor = if (expanded) A11yTokens.InkMuted else A11yTokens.Link
                    )
                    if (expanded) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            yearMonths.forEach { ym ->
                                MenuMonthPastille(
                                    ym = ym,
                                    selected = ym == selectedYearMonth,
                                    onClick = {
                                        onSelectMonth(ym)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isPastMonth || isFuturePreview) {
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryA11yButton(
                    text = "Mois en cours",
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

@Composable
private fun MenuMonthPastille(
    ym: YearMonth,
    selected: Boolean,
    onClick: () -> Unit
) {
    val chipLabel = DayLabelFormat.monthChipShort(ym)
    val chipShape = StampShape
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
                ) { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}
