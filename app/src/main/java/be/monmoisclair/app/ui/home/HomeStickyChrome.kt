package be.monmoisclair.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.theme.HomeTokens

/**
 * Chip outil Accueil 7.13 — well tonal + icône brand ≥26 dp + libellé, ≥48 dp.
 */
@Composable
internal fun StickyIconTool(
    iconRes: Int,
    label: String,
    chipColor: Color,
    onClickLabel: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * true = drawable brand multi-couches (teal/or/forêt) — pas de tint mono.
     * false = mono teinté [iconTint].
     */
    brandColoredIcon: Boolean = true,
    /** Teal courses / forêt épargne — ignoré si [brandColoredIcon]. */
    iconTint: Color = HomeTokens.Link
) {
    Row(
        modifier = modifier
            .widthIn(min = HomeTokens.minTouch)
            .heightIn(min = HomeTokens.minTouch)
            .clip(HomeTokens.wellRadius)
            .background(chipColor)
            .clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(HomeTokens.chipRadius)
                .background(HomeTokens.HeroSurface.copy(alpha = 0.55f))
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (brandColoredIcon) Color.Unspecified else iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = HomeTokens.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

/**
 * 7.13 : Courses | Mettre de côté en grille sous le montant (icon wells).
 */
@Composable
internal fun StickyHeroTools(
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
            StickyIconTool(
                iconRes = R.drawable.ic_cart,
                label = coursesLabel,
                chipColor = HomeTokens.CoursesChip,
                onClickLabel = "Modifier les courses",
                contentDescription = coursesDescription ?: coursesLabel,
                onClick = onOpenCourses,
                modifier = Modifier.weight(1f),
                iconTint = HomeTokens.Link
            )
        }
        if (savingsLabel != null) {
            StickyIconTool(
                iconRes = R.drawable.ic_piggy,
                label = savingsLabel,
                chipColor = HomeTokens.SavingsChip,
                onClickLabel = "Modifier mettre de côté",
                contentDescription = savingsDescription ?: savingsLabel,
                onClick = onOpenSavings,
                modifier = Modifier.weight(1f),
                iconTint = HomeTokens.Ok
            )
        }
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
            .clip(HomeTokens.chipRadius)
            .background(
                if (enabled) HomeTokens.Chip else HomeTokens.Chip.copy(alpha = 0.45f)
            )
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (enabled) HomeTokens.InkMuted else HomeTokens.InkMuted.copy(alpha = 0.35f),
            modifier = Modifier.size(26.dp)
        )
    }
}
