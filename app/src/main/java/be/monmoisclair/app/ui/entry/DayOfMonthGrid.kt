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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val fontScale = LocalDensity.current.fontScale
    // Écart serré : 31 cases doivent pouvoir défiler sans masquer Enregistrer.
    val dayGap = if (fit.compactHomeChrome) 4.dp else 6.dp
    // P1-4 : gros texte système — glyphe plus petit pour éviter clip dans 48 dp.
    val dayTextStyle = if (fontScale >= 1.3f) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.labelLarge
    }
    Column(modifier = modifier) {
        // SectionTitle = titleMedium + InkMuted (comme Fréquence / Dossiers).
        Text(
            text = "Jour du mois",
            style = MaterialTheme.typography.titleMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier.padding(bottom = if (fit.compactHomeChrome) 4.dp else 6.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dayGap),
            verticalArrangement = Arrangement.spacedBy(dayGap)
        ) {
            for (day in 1..31) {
                val selected = selectedDay == day
                // GRID01 : tampon date = capsule, pas case 16 dp.
                val shape = StampShape
                val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
                val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        // clickable Material gonflait à ~64 dp (campagne Ultra).
                        // size + clickable(indication=null) garde exactement 48 dp.
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
                            onClick = { onSelect(day) }
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
