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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape

@Composable
fun HealthStatusRow(label: String, ok: Boolean) {
    val status = if (ok) "Prêt" else "À régler"
    val shape = MaterialTheme.shapes.medium
    // HLTH01 : pastilles Prêt / À régler = capsule.
    val chipShape = StampShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .border(1.dp, A11yTokens.PaperBorder, shape)
            .background(A11yTokens.PaperCard, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label : $status"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = status,
            color = if (ok) A11yTokens.AccentOkInk else A11yTokens.AccentWarn,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .background(
                    if (ok) A11yTokens.AccentOkSoft else A11yTokens.PaperWarm,
                    chipShape
                )
                .then(
                    if (ok) Modifier else Modifier.border(1.dp, A11yTokens.AccentWarn, chipShape)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.Ink
        )
    }
}
