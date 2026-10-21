package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * Les mois courts clampent en silence côté moteur — on le dit sous la grille.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayOfMonthGrid(
    selectedDay: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = "Jour du mois",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted
        )
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (day in 1..31) {
                val selected = selectedDay == day
                val shape = MaterialTheme.shapes.medium
                val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
                val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(shape)
                        .background(bg, shape)
                        .border(
                            1.dp,
                            if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                            shape
                        )
                        .semantics {
                            contentDescription = if (selected) {
                                "Jour $day, sélectionné"
                            } else {
                                "Jour $day"
                            }
                        }
                        .selectable(
                            selected = selected,
                            enabled = enabled,
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
        if (selectedDay != null && selectedDay > 28) {
            Spacer(Modifier.height(8.dp))
            val tipShape = MaterialTheme.shapes.medium
            Text(
                text = "En février (et les mois courts), ce sera le dernier jour du mois.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, tipShape)
                    .background(A11yTokens.PaperCard, tipShape)
                    .padding(12.dp)
            )
        }
    }
}
