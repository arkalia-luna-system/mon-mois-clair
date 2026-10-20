package be.monmoisclair.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import java.time.YearMonth

/**
 * Hub d’outils hors ledger — calendrier mois + raccourcis.
 * Les futures fonctions s’ajoutent ici, pas sur le sticky Home.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeMenuSheet(
    selectedYearMonth: YearMonth,
    isPastMonth: Boolean,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onGoToCurrentMonth: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val current = AppClock.currentYearMonth()
    val months = HomeMonthChoices.recentMonths(current)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = A11yTokens.PaperCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = A11yTokens.screenPadding)
                .padding(bottom = 28.dp)
        ) {
            Text(text = "Menu", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Un mois, ou un outil.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Mois", style = MaterialTheme.typography.titleLarge)
            Text(
                text = current.year.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                months.forEach { ym ->
                    val selected = ym == selectedYearMonth
                    Surface(
                        color = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.heightIn(min = A11yTokens.minTouchHeight)
                    ) {
                        Text(
                            text = DayLabelFormat.monthChip(ym, current),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) A11yTokens.ButtonText else A11yTokens.Ink,
                            modifier = Modifier
                                .clickable {
                                    onSelectMonth(ym)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            if (isPastMonth) {
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryA11yButton(
                    text = "Revenir au mois en cours",
                    onClick = {
                        onGoToCurrentMonth()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Outils", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryA11yButton(
                text = "Vérifier les alertes",
                onClick = {
                    onDismiss()
                    onOpenHealth()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryA11yButton(
                text = "Réglages",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
