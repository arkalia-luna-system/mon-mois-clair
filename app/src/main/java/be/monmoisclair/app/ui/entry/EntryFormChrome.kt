package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/** Titre de section repliable — titleLarge, pas un Secondary plein largeur. */
@Composable
internal fun FormSectionToggle(
    title: String,
    expanded: Boolean,
    closedHint: String,
    onToggle: () -> Unit
) {
    val label = if (expanded) "$title ▴" else "$title ▾"
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) "Replier $title" else "Déplier $title",
                    onClick = onToggle
                )
                .padding(vertical = 10.dp)
                .semantics { contentDescription = label }
        )
        // Fermé : hint utile. Ouvert : le ▴ suffit (pas de « Ouvert » redondant).
        if (!expanded) {
            Text(
                text = closedHint,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
    }
}

@Composable
internal fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val fit = rememberDisplayFit()
    val hPad = if (fit.compactHomeChrome) 12.dp else 16.dp
    val vPad = if (fit.compactHomeChrome) 10.dp else 12.dp
    // FORM02 : choix exclusifs = pastille capsule, pas bouton 16 dp.
    val shape = StampShape
    val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
    val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(shape)
            .background(bg, shape)
            .border(
                1.dp,
                if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                shape
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = A11yTokens.minTouchHeight)
            .padding(horizontal = hPad, vertical = vPad)
            .semantics {
                contentDescription = if (selected) "$text, sélectionné" else text
            }
    )
}

@Composable
internal fun CheckRow(checked: Boolean, label: String, onChecked: (Boolean) -> Unit) {
    // Checkbox avec onCheckedChange réel : le pattern toggleable+null
    // n’enregistre pas les taps sur One UI / uiautomator (case « Revient… » morte).
    // Pas de carte bordée : rythme crème + case teal (FORM07).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Checkbox) { onChecked(!checked) }
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = if (checked) "$label, coché" else "$label, non coché"
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FORM07 : case teal carnet (pas Material stock).
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = A11yTokens.ButtonFill,
                uncheckedColor = A11yTokens.FieldLine,
                checkmarkColor = A11yTokens.ButtonText
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.Ink
        )
    }
}
