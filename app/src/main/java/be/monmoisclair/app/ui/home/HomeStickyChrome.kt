package be.monmoisclair.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Cellule outil sticky (Courses / épargne) — demi-largeur, ≥48 dp, › discret.
 * Remplace deux StickyLinkRow empilées pour libérer la liste.
 */
@Composable
internal fun StickyToolCell(
    label: String,
    labelColor: Color,
    onClickLabel: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
            .padding(vertical = 2.dp, horizontal = 2.dp)
            .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = A11yTokens.InkMuted
        )
    }
}

/** Rangée secondaire : Courses | Mettre de côté (une seule ligne de hauteur). */
@Composable
internal fun StickyToolsRow(
    coursesLabel: String?,
    coursesDescription: String?,
    onOpenCourses: () -> Unit,
    savingsLabel: String?,
    savingsDescription: String?,
    onOpenSavings: () -> Unit
) {
    if (coursesLabel == null && savingsLabel == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (coursesLabel != null) {
            StickyToolCell(
                label = coursesLabel,
                labelColor = A11yTokens.InkMuted,
                onClickLabel = "Modifier les courses",
                contentDescription = coursesDescription ?: coursesLabel,
                onClick = onOpenCourses,
                modifier = Modifier.weight(1f)
            )
        }
        if (savingsLabel != null) {
            StickyToolCell(
                label = savingsLabel,
                labelColor = A11yTokens.InkMuted,
                onClickLabel = "Modifier mettre de côté",
                contentDescription = savingsDescription ?: savingsLabel,
                onClick = onOpenSavings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Ligne sticky pleine largeur (legacy / disclosure) — padding serré, ellipsis, › discret. */
@Composable
internal fun StickyLinkRow(
    label: String,
    labelColor: Color,
    compact: Boolean,
    onClickLabel: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
            .padding(vertical = if (compact) 2.dp else 4.dp)
            .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = A11yTokens.InkMuted
        )
    }
}

@Composable
internal fun MonthStep(
    iconRes: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (enabled) A11yTokens.InkMuted else A11yTokens.InkMuted.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}
