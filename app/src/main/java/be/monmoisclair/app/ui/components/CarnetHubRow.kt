package be.monmoisclair.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Ligne hub 7.13 : carte soft + well tonal leading + titre + › , cible ≥ 48 dp.
 * Même rythme Menu Accueil / hub Réglages.
 */
@Composable
fun CarnetHubRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = A11yTokens.Ink,
    chevronColor: Color = A11yTokens.InkMuted,
    showDivider: Boolean = true,
    leadingIconRes: Int? = null,
    /** true = PNG kit or/teal (pas de tint mono). */
    leadingColoredIcon: Boolean = false
) {
    val shape = MaterialTheme.shapes.medium
    if (showDivider) {
        Spacer(modifier = Modifier.padding(top = 8.dp))
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(A11yTokens.PaperCard)
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (leadingIconRes != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(A11yTokens.PaperWarm)
            ) {
                Icon(
                    painter = painterResource(leadingIconRes),
                    contentDescription = null,
                    tint = if (leadingColoredIcon) Color.Unspecified else A11yTokens.Link,
                    modifier = Modifier.size(if (leadingColoredIcon) 26.dp else 22.dp)
                )
            }
        }
        Text(
            text = label,
            style = CarnetTitleTokens.hubRowLabelStyle(MaterialTheme.typography),
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = chevronColor
        )
    }
}
