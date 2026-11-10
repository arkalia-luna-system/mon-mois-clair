package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Jours 1–31 en cibles ≥ 48 dp.
 * 7.07 : si un jour est déjà choisi, grille repliée (chip « Jour N ») —
 * sinon mur de 5 rangées dès l’ouverture (audit fiche P0).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayOfMonthGrid(
    selectedDay: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onInteract: () -> Unit = {}
) {
    val fit = rememberDisplayFit()
    val fontScale = LocalDensity.current.fontScale
    val dayGap = if (fit.compactHomeChrome) 4.dp else 6.dp
    val dayTextStyle = if (fontScale >= 1.3f) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.labelLarge
    }
    // Replié si jour déjà choisi ; ouvert si aucun choix (Add).
    var expanded by remember(selectedDay == null) {
        mutableStateOf(selectedDay == null)
    }
    Column(modifier = modifier) {
        Text(
            text = "Jour du mois",
            style = MaterialTheme.typography.titleMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier.padding(bottom = if (fit.compactHomeChrome) 4.dp else 6.dp)
        )
        if (!expanded && selectedDay != null) {
            Text(
                text = "Jour $selectedDay ▾",
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) A11yTokens.Link else A11yTokens.InkMuted.copy(alpha = 0.55f),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                        onClickLabel = "Changer le jour du mois"
                    ) {
                        onInteract()
                        expanded = true
                    }
                    .semantics {
                        contentDescription = "Jour $selectedDay sélectionné. Toucher pour changer."
                    }
                    .padding(vertical = 2.dp)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dayGap),
                verticalArrangement = Arrangement.spacedBy(dayGap)
            ) {
                for (day in 1..31) {
                    val selected = selectedDay == day
                    val shape = StampShape
                    val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
                    val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(A11yTokens.minTouchHeight)
                            .alpha(if (enabled) 1f else 0.45f)
                            .clip(shape)
                            .background(bg, shape)
                            .then(
                                if (selected) {
                                    Modifier
                                } else {
                                    Modifier.border(1.dp, A11yTokens.PaperBorder, shape)
                                }
                            )
                            .semantics {
                                this.selected = selected
                                contentDescription = when {
                                    !enabled -> "Jour $day, indisponible"
                                    selected -> "Jour $day, sélectionné"
                                    else -> "Jour $day"
                                }
                            }
                            .clickable(
                                enabled = enabled,
                                role = Role.RadioButton,
                                indication = null,
                                interactionSource = null,
                                onClick = {
                                    onInteract()
                                    onSelect(day)
                                    expanded = false
                                }
                            )
                    ) {
                        Text(
                            text = day.toString(),
                            color = fg,
                            style = dayTextStyle,
                            maxLines = 1
                        )
                    }
                }
            }
            if (selectedDay != null) {
                Text(
                    text = "Replier",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.Link,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Replier la grille des jours"
                        ) {
                            onInteract()
                            expanded = false
                        }
                )
            }
        }
        if (selectedDay != null && selectedDay > 28) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Mois court : dernier jour du mois.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
