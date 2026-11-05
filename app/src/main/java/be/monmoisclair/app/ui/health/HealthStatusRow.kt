package be.monmoisclair.app.ui.health

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape

/** Ligne Alertes 7.11 — carte soft + pastille filled (plus de filet plat). */
@Composable
fun HealthStatusRow(label: String, ok: Boolean) {
    val status = if (ok) "Prêt" else "À régler"
    val chipShape = StampShape
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(A11yTokens.PaperCard)
            .heightIn(min = A11yTokens.minTouchHeight)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label : $status"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // FR scan : libellé d’abord, pastille statut à droite.
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = status,
            color = if (ok) A11yTokens.AccentOkInk else A11yTokens.AccentWarn,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .then(
                    if (ok) {
                        Modifier
                    } else {
                        Modifier.border(
                            1.dp,
                            A11yTokens.AccentWarn.copy(alpha = 0.45f),
                            chipShape
                        )
                    }
                )
                .background(
                    if (ok) A11yTokens.AccentOkSoft else A11yTokens.PaperWarm,
                    chipShape
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
