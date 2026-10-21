package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Jours 1–31 en cibles ≥ 48 dp, sans grossir au-delà (sinon la grille
 * mange Enregistrer / force un demi-rangée coupée sous le pli Ultra).
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
    val fit = rememberDisplayFit()
    // Écart serré : 31 cases doivent pouvoir défiler sans masquer Enregistrer.
    val dayGap = if (fit.compactHomeChrome) 4.dp else 6.dp
    val tipPad = if (fit.compactHomeChrome) 10.dp else 12.dp
    Column(modifier = modifier) {
        Text(
            text = "Jour du mois",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted
        )
        Spacer(Modifier.height(if (fit.compactHomeChrome) 4.dp else 6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dayGap),
            verticalArrangement = Arrangement.spacedBy(dayGap)
        ) {
            for (day in 1..31) {
                val selected = selectedDay == day
                val shape = MaterialTheme.shapes.medium
                val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
                val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        // Taille stricte 48 dp (pas defaultMinSize : selectable/texte
                        // gonflaient à ~64 dp → rangée coupée sous Enregistrer).
                        .size(A11yTokens.minTouchHeight)
                        .alpha(if (enabled) 1f else 0.45f)
                        .clip(shape)
                        .background(bg, shape)
                        .border(
                            1.dp,
                            if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                            shape
                        )
                        .semantics {
                            contentDescription = when {
                                !enabled -> "Jour $day, indisponible"
                                selected -> "Jour $day, sélectionné"
                                else -> "Jour $day"
                            }
                        }
                        .selectable(
                            selected = selected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelect(day) }
                        )
                ) {
                    Text(
                        text = day.toString(),
                        color = fg,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        Text(
            text = "Faites défiler pour voir tous les jours (1 à 31).",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier.padding(top = 6.dp)
        )
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
                    .padding(tipPad)
            )
        }
    }
}
