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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSheetHandle
import be.monmoisclair.app.ui.components.CarnetTitleTokens
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import be.monmoisclair.app.ui.theme.rememberSheetBottomClearance
import java.time.YearMonth

/**
 * Feuille « Choisir un mois » — carnet (années + pastilles), pas agenda Google ni grille 31 jours.
 * Horizon ~2 ans passé + futur ([HomeMonthChoices.calendarByYear]).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeMonthPickerSheet(
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onGoToCurrentMonth: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val current = AppClock.currentYearMonth()
    val byYear = remember(current) { HomeMonthChoices.calendarByYear(current) }
    val fit = rememberDisplayFit()
    val sheetBottom = rememberSheetBottomClearance()
    var expandedYear by remember(selectedYearMonth, byYear) {
        mutableStateOf(selectedYearMonth.year)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = { CarnetSheetHandle() },
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
            CarnetPageTitle("Choisir un mois", withTopSpacer = false)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Passé et à venir (~2 ans). Futur = aperçu, sans FAIT.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 12.dp else 16.dp))

            byYear.forEach { (year, yearMonths) ->
                val expanded = expandedYear == year
                HorizontalDivider(color = A11yTokens.PaperBorder)
                Text(
                    text = if (expanded) "$year ▾" else "$year ›",
                    style = CarnetTitleTokens.sectionTitleStyle(MaterialTheme.typography),
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (expanded) "Replier $year" else "Déplier $year"
                        ) {
                            expandedYear = if (expanded) -1 else year
                        }
                        .padding(vertical = 10.dp)
                        .semantics {
                            contentDescription = if (expanded) {
                                "Année $year, ouverte"
                            } else {
                                "Année $year, fermée"
                            }
                        }
                )
                if (expanded) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        yearMonths.forEach { ym ->
                            MonthPastille(
                                ym = ym,
                                selected = ym == selectedYearMonth,
                                isCurrent = ym == current,
                                onClick = {
                                    onSelectMonth(ym)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }

            if (selectedYearMonth != current) {
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
private fun MonthPastille(
    ym: YearMonth,
    selected: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val chipLabel = DayLabelFormat.monthChipShort(ym)
    val chipShape = StampShape
    val borderColor = when {
        selected -> A11yTokens.ButtonFill
        isCurrent -> A11yTokens.Link
        else -> A11yTokens.PaperBorder
    }
    Surface(
        color = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm,
        shape = chipShape,
        modifier = Modifier
            .heightIn(min = A11yTokens.minTouchHeight)
            .border(1.dp, borderColor, chipShape)
            .semantics(mergeDescendants = true) {
                contentDescription = when {
                    selected -> "Mois affiché : $chipLabel ${ym.year}"
                    isCurrent -> "Mois en cours : $chipLabel. Aller à $chipLabel"
                    else -> "Aller à $chipLabel ${ym.year}"
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
                    onClickLabel = "Afficher $chipLabel ${ym.year}"
                ) { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}
