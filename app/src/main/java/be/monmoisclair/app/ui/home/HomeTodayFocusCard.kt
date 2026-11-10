package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.domain.HomeTodayFocus
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.theme.HomeTokens

/**
 * Raccourci Accueil — ce qui est urgent aujourd’hui (retard + échéance du jour).
 */
@Composable
fun HomeTodayFocusCard(
    focus: HomeTodayFocus.Result,
    onShowFirst: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!focus.hasAction) return
    val shape = HomeTokens.chipRadius
    val hasOverdue = focus.overdue.isNotEmpty()
    val bg = HomeTokens.Chip
    val border = if (hasOverdue) HomeTokens.DangerFill else HomeTokens.CardBorder
    val borderWidth = HomeTodayFocusCardStyle.overdueBorderWidthDp(hasOverdue).dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(borderWidth, border, shape)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Pour aujourd’hui",
            style = MaterialTheme.typography.titleSmall,
            color = HomeTokens.Ink
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = focus.headline(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (focus.overdue.isNotEmpty()) HomeTokens.Danger else HomeTokens.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .semantics {
                        contentDescription = focus.contentDescription()
                    }
            )
            QuietLinkA11y(
                text = "Voir",
                onClick = onShowFirst,
                fillMaxWidth = false,
                onClickLabel = "Aller à la première facture à traiter",
                modifier = Modifier
                    .heightIn(min = HomeTokens.minTouch)
                    .padding(start = 8.dp)
            )
        }
        Text(
            text = "En retard ou échéance aujourd’hui — cochez FAIT après le virement.",
            style = MaterialTheme.typography.bodyMedium,
            color = HomeTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
