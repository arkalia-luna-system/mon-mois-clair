package be.monmoisclair.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Ligne hub 7.12 : carte soft + leading icon optionnel + titre + › , cible ≥ 48 dp.
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
    leadingIconRes: Int? = null
) {
    val shape = MaterialTheme.shapes.medium
    if (showDivider) {
        // Espace entre cartes (plus de filet plat carnet).
        Spacer(modifier = Modifier.padding(top = 6.dp))
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(A11yTokens.PaperCard)
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (leadingIconRes != null) {
            Icon(
                painter = painterResource(leadingIconRes),
                contentDescription = null,
                tint = A11yTokens.Link,
                modifier = Modifier.size(22.dp)
            )
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
