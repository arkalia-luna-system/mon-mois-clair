package be.monmoisclair.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.theme.A11yTokens

/**
 * Le même M que l’icône, plus le nom.
 * Réservé aux écrans qui présentent l’application (verrou, départ, réglages, aide).
 * Pas sur le mois : l’accueil reste le carnet.
 */
@Composable
fun AppWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 32.dp,
    announce: Boolean = true
) {
    val spoken = if (announce) {
        Modifier.semantics { contentDescription = "Mon Mois Clair" }
    } else {
        Modifier.clearAndSetSemantics {}
    }
    Row(
        modifier = modifier.then(spoken),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_brand_m),
            contentDescription = null,
            modifier = Modifier.size(markSize)
        )
        Text(
            text = "Mon Mois Clair",
            style = MaterialTheme.typography.labelLarge,
            color = A11yTokens.Ink
        )
    }
}
