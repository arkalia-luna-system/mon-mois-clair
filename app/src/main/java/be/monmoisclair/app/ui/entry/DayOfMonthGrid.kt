package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Jours 1–31 en grosses cibles (≥ 48 dp).
 * Sur un S26 (~360 dp), 7 colonnes de 48 dp ne tiennent pas : la grille se replie.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayOfMonthGrid(
    selectedDay: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(text = "Jour du mois", style = MaterialTheme.typography.bodyLarge)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (day in 1..31) {
            val selected = selectedDay == day
            val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
            val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(bg)
                    .semantics {
                        contentDescription = if (selected) {
                            "Jour $day, sélectionné"
                        } else {
                            "Jour $day"
                        }
                    }
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(day) }
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = day.toString(),
                    color = fg,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
