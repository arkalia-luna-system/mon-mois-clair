package be.monmoisclair.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Tip mist unique (7.29) — PaperWarm + forme medium + padding 14×12.
 * Remplace les ~16 variantes tip/carte inventées écran par écran.
 */
@Composable
fun CarnetTipCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = A11yTokens.InkMuted,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(0.5.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
            .background(A11yTokens.PaperWarm)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}
